package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Element;


import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunker;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.openqa.test.team02.utilities.XMLParser;

import org.dom4j.DocumentException;


public class KeytermExtractor extends AbstractKeytermExtractor {

  public static final String PARAM_MODELFILE = "model_file";

  private Chunker chunker;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    File modelFile = new File(((String) c.getConfigParameterValue(PARAM_MODELFILE)).trim());
    if (!modelFile.exists()) {
      throw new ResourceInitializationException();
    }

    try {
      chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  } 

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    
 // stemming + stop word removal
    TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
    TokenizerFactory f2 = new LowerCaseTokenizerFactory(f1);
    TokenizerFactory f3 = new PorterStemmerTokenizerFactory(f2);
    TokenizerFactory f4 = new EnglishStopTokenizerFactory(f3);
    TokenizerFactory f5 = new RegExFilteredTokenizerFactory(f4, Pattern.compile("[a-z0-9]+"));
    Tokenizer nowTokenizer = f5.tokenizer(question.toCharArray(), 0, question.length());
    String[] words = nowTokenizer.tokenize();
    
    List<Keyterm> keytermList = new ArrayList<Keyterm>();
    String s = new String("http://swissvar.expasy.org/cgi-bin/swissvar/result?format=xml&global_textfield=");
    URL u;
    XMLParser x = new XMLParser();
    for (String word : words) {
      String keyterm = word;
      try {
        u = new URL(s + keyterm);
        List l = x.getVariants(x.parse(u));
        if(l != null){
          l = l.subList(0, Math.min(5, l.size()));
          for(Object o:l){
              String v = ((Element) o).getText();
              System.out.println(v);
              keytermList.add(new Keyterm(v));
            }
        }

      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (DocumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      keytermList.add(new Keyterm(keyterm));
    }

    return keytermList;
  }
}
