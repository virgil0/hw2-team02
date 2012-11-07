package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;

import hw1.kmandavi.CSVToHashMap;
import hw1.kmandavi.GeneTag;
import hw1.kmandavi.PosTagNamedEntityRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class KmandaviKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    // TODO Auto-generated method stub
    CSVToHashMap genes = new CSVToHashMap();

    PosTagNamedEntityRecognizer Tagger = null;
    try {
      Tagger = new PosTagNamedEntityRecognizer();
    } catch (ResourceInitializationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Map<Integer, Integer> occurences = Tagger.getGeneSpans(question);
    List<Keyterm> keyterms = new ArrayList<Keyterm>();

    int begin;
    int end;
    String gene;

    for (Map.Entry<Integer, Integer> entry : occurences.entrySet())
    {
      begin = entry.getKey();
      end = entry.getValue();
      gene = question.substring(begin, end);
      
      if(genes.findGene(gene) == true ){
        keyterms.add(new Keyterm(gene));

      }
    }

    return keyterms;
  }

}
