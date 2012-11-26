package edu.cmu.lti.oaqa.openqa.test.team02.passage;


import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;

public class SimplePassageCandidateFinder {
  private String text;
  private String docId;
  
  private int textSize;      // values for the entire text
  private int totalMatches;  
  private int totalKeyterms;
  
  private KeytermWindowScorer scorer;
  
  public SimplePassageCandidateFinder( String docId , String text , KeytermWindowScorer scorer ) {
    super();
    this.text = text;
    this.docId = docId;
    this.textSize = text.length();
    this.scorer = scorer;
  }
  
  public List<String> getSentences(String text){
    BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
    iterator.setText(text);
    int start = iterator.first();
    List<String> sentences = new ArrayList<String>();
    for(int end = iterator.next(); end != BreakIterator.DONE; start = end, end =iterator.next()){
      sentences.add(text.substring(start,end));
    }
    return sentences;
  }
  public String[] getWords(String text){
    String[] words = text.split("[\\W]");
    return words;
  }

  public List<PassageCandidate> extractPassages( String[] keyterms ) {
    List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    List<String> sentences = new ArrayList<String>();

    BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
    iterator.setText(text);
    int start = iterator.first();
    for(int end = iterator.next(); end != BreakIterator.DONE; start = end, end =iterator.next()){
      sentences.add(text.substring(start,end));
    }
    List<PassageSpan> passages = new ArrayList<PassageSpan>();
    int cur = 0;
    //System.out.println(sentences);
    //System.out.println(text.length());
    while(sentences.size() > 0){
      PassageSpan passage = new PassageSpan(0,0);
      while( getSentences(text.substring(passage.getBegin(),passage.getEnd())).size() < 3 || getWords(text.substring(passage.getBegin(),passage.getEnd())).length < 45){
        int length = getWords(sentences.get(0)).length + getWords(sentences.get(1)).length + getWords(sentences.get(2)).length ;
        int l0 = sentences.get(0).length();
        int l1 = sentences.get(1).length();
        int l2 = sentences.get(2).length();
        //System.out.println(length);
        if( length <=45 ){
          passage.setBegin(cur);
          passage.setEnd(Math.min(text.length(),cur + l1 + l2 + l0 + 1));
          cur = Math.min(text.length(),cur + l1 + l2 + l0 +2 );
        }
        else if(length - getWords(sentences.get(2)).length <= 45){
          passage.setBegin(cur);
          passage.setEnd(Math.min(cur + l1 + l2 + l0, text.length()));
          cur = Math.min(text.length(),cur + l1 + l2 + l0 + 2);
        }
        else if(length - getWords(sentences.get(2)).length - getWords(sentences.get(1)).length<= 45 ){
          passage.setBegin(cur);
          passage.setEnd(Math.min(cur + l0 + l1,text.length())); 
          cur =  Math.min(cur + l0 + l1,text.length());
        }
        else{
          passage.setBegin(cur);
          passage.setEnd(Math.min(cur + l0, text.length()));
          cur =  Math.min(cur + l0, text.length());
        }
        if(cur >= text.length())
          break;
      }
      if(cur >= text.length())
        break;
      System.out.println("passage:"  + text.substring(passage.getBegin(),passage.getEnd()));
      if( getSentences(text.substring(passage.getBegin(),passage.getEnd())).size() > 1 ){
        sentences.remove(0);
      }
      if(getSentences(text.substring(passage.getBegin(),passage.getEnd())).size() == 3){
        sentences.remove(1);
      }     
      passages.add(passage);
    }
      
    
    
    for(PassageSpan passage: passages){
      String t = text.substring(passage.getBegin(),passage.getEnd());    
      // Find all keyterm matches.
      for ( String keyterm : keyterms ) {
        PassageCandidate window = null;
        Pattern p = Pattern.compile( keyterm );
        Matcher m = p.matcher( t );
        if ( m.find() ) {
          //matchedSpans.add( passage );
          try {
            window = new PassageCandidate( docId , passage.getBegin() , passage.getEnd() , -1 , null );
          } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
          }
          result.add( window );

        }

      }
    }

    /*
    // For every possible window, calculate keyterms found, matches found; score window, and create passage candidate.
   // for ( Integer begin : leftEdges ) {
     // for ( Integer end : rightEdges ) {
       // if ( end <= begin ) continue; 
        // This code runs for each window.
        int keytermsFound = 0;
        int matchesFound = 0;
        for ( List<PassageSpan> keytermMatches : matchingSpans ) {
          boolean thisKeytermFound = false;
          for ( PassageSpan keytermMatch : keytermMatches ) {
            if ( keytermMatch.containedIn( keytermMatch.getBegin() , keytermMatch.getEnd() ) ){
              matchesFound++;
              thisKeytermFound = true;
            }
          }
          if ( thisKeytermFound ) keytermsFound++;
        }
        double score = scorer.scoreWindow( begin , end , matchesFound , totalMatches , keytermsFound , totalKeyterms , textSize );
        PassageCandidate window = null;
        try {
          window = new PassageCandidate( docId , begin , end , (float) score , null );
        } catch (AnalysisEngineProcessException e) {
          e.printStackTrace();
        }
        result.add( window );
      }
    }
    */
    
    // Sort the result in order of decreasing score.
    // Collections.sort ( result , new PassageCandidateComparator() );
    return result;

  }
  private class PassageCandidateComparator implements Comparator {
    // Ranks by score, decreasing.
    public int compare( Object o1 , Object o2 ) {
      PassageCandidate s1 = (PassageCandidate)o1;
      PassageCandidate s2 = (PassageCandidate)o2;
      if ( s1.getProbability() < s2.getProbability() ) {
        return 1;
      } else if ( s1.getProbability() > s2.getProbability() ) {
        return -1;
      }
      return 0;
    }   
  }

  class PassageSpan {
    private int begin, end;
    public PassageSpan( int begin , int end ) {
      this.begin = begin;
      this.end = end;
    }
    public boolean containedIn ( int begin , int end ) {
      if ( begin <= this.begin && end >= this.end ) {
        return true;
      } else {
        return false;
      }
    }
    public int getBegin(){
      return this.begin;
    }
    public int getEnd(){
      return this.end;
    }
    public void setBegin(int begin){
      this.begin = begin;
    }
    public void setEnd(int end){
      this.end = end;
    }
  }
  

}