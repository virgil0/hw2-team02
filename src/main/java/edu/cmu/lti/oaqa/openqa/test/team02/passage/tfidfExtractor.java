package edu.cmu.lti.oaqa.openqa.test.team02.passage;

import java.text.BreakIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.List;



import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import org.jsoup.Jsoup;

import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.tokenizer.LowerCaseTokenizerFactory;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;

import com.google.common.base.Function;

import com.google.common.collect.Lists;



import edu.cmu.lti.oaqa.framework.data.Keyterm;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;

import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;

import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;





public class tfidfExtractor extends SimplePassageExtractor {

  private class sentencePR
  {
    public sentencePR(String content_, int start_, int end_)
    {
      content = content_;
      start = start_;
      end = end_;
      len = end - start;
      
    }
    public String content;
    public int start;
    public int end;
    public int len;
    public double score;
  }

  private void mapAdd(Map map_t, String word)
  {             
       
    if(map_t.containsKey(word))
    {
      int v = (Integer) map_t.get(word);
      v++;
      map_t.put(word, v);
    }
    else
    {
      map_t.put(word, 1);
    }
  }
  @Override

  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,

          List<RetrievalResult> documents) {

    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    for (RetrievalResult document : documents) {

      System.out.println("RetrievalResult: " + document.toString());

      String id = document.getDocID();

      Map<String, Integer> documentMap = new HashMap<String, Integer>();
      try {

        String htmlText = wrapper.getDocText(id);


        String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;

        //tdf
        int wordCount = 0;
        int keyCount = 0;
        double tdf = 0;
        BreakIterator wholeWord = BreakIterator.getWordInstance();
    //    sentenceArr.get(i).score = 0;
     //   String theText = sentenceArr.get(i).content;
        wholeWord.setText(text);
        int ss = wholeWord.first();
    //    Map<String, Integer> map = new HashMap<String, Integer>();
        for (int end = wholeWord.next(); end != BreakIterator.DONE; ss = end, end = wholeWord.next()) {
             String mm = text.substring(ss,end);
              mapAdd(documentMap, mm);
              wordCount++;
        }
        for(int j = 0; j < keyterms.size(); j++)
        {
            if(documentMap.containsKey(keyterms.get(j).toString()))
            {
              int xx = documentMap.get(keyterms.get(j).toString());
              keyCount += xx;
            }
        }
        
        tdf = Math.log((double)wordCount / keyCount);

      //if
        BreakIterator boundary = BreakIterator.getSentenceInstance();

        boundary.setText(text);

        int start = boundary.first();
        int lastStart = -1;
        int last2Start = -1;
    
        ArrayList<sentencePR> sentenceArr = new ArrayList<sentencePR>();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
             if(end - start > 30)
             {
               int offset = 5;
               if (start > offset && end < BreakIterator.DONE - offset)
               {
                   sentenceArr.add(new sentencePR(text.substring(start - offset,end + offset) , start - offset, end + offset));
               }
               else
               {
                 sentenceArr.add(new sentencePR(text.substring(start,end) , start, end));
               }
             }
        }

        //compute the tf for each sentence
        for(int i = 0; i < sentenceArr.size(); i++)
        {
             BreakIterator sepWord = BreakIterator.getWordInstance();
             sentenceArr.get(i).score = 1;
             String theText = sentenceArr.get(i).content;
             sepWord.setText(theText);
             int begin = sepWord.first();
             Map<String, Integer> map = new HashMap<String, Integer>();
             for (int end = sepWord.next(); end != BreakIterator.DONE; begin = end, end = sepWord.next()) {
                  String mm = theText.substring(begin,end);
                   mapAdd(map, mm);
             }
             for(int j = 0; j < keyterms.size(); j++)
             {
                 if(map.containsKey(keyterms.get(j).toString()))
                 {
                   int keytermCount = map.get(keyterms.get(j).toString());
                   if(keytermCount > 0)
                   {
                        double temp = (double)keytermCount / sentenceArr.get(i).len;
                        sentenceArr.get(i).score *= temp;
                   }
                   else
                   {
                         sentenceArr.get(i).score *=0.5 / sentenceArr.get(i).len;
                   }
                 }
             }
             double ssaa = sentenceArr.get(i).score;
             PassageCandidate senWithScore = new PassageCandidate( id ,sentenceArr.get(i).start , sentenceArr.get(i).end ,
                      (float) (sentenceArr.get(i).score * tdf) , null );
             
             result.add(senWithScore);
        }
        //
        
      } catch (SolrServerException e) {

        e.printStackTrace();

      } catch (AnalysisEngineProcessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    return result;

  }



}