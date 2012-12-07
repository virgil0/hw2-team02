package edu.cmu.lti.oaqa.openqa.test.team02.passage;


import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;

public class PassageExtractor extends SimplePassageExtractor {
  
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
      try {
        String htmlText = wrapper.getDocText(id);

        // cleaning HTML text
        String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        // for now, making sure the text isn't too long
        //text = text.substring(0, Math.min(5000, text.length()));
        //text = text.substring(0, Math.min(5000, text.length()));
        System.out.println(text);

        PassageCandidateFinder finder = new PassageCandidateFinder(id, text, new KeytermWindowScorerSum());
        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
        List<PassageCandidate> passageSpans = finder.extractPassages(keytermStrings.toArray(new String[0]));
        
        for(int i = 0; i < passageSpans.size(); i++)
        {
             double passageScore = 0;
             BreakIterator sepWord = BreakIterator.getWordInstance();
             String theText = text.substring(passageSpans.get(i).getStart(), passageSpans.get(i).getEnd());
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
                        double temp = (double)keytermCount / theText.length();
                        passageScore +=temp;
                   }
                   else
                   {
                      //   passageScore += 0.5 / theText.length();
                   }
                 }
             }
             PassageCandidate senWithScore = new PassageCandidate( id ,passageSpans.get(i).getStart(), passageSpans.get(i).getEnd(),
                      (float) (passageScore) , null );
        
          result.add(senWithScore);
        }
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
