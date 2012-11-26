package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.Node;


import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.openqa.test.team02.utilities.XMLParser;

import org.dom4j.DocumentException;


public class KeytermExtractor extends AbstractKeytermExtractor {

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
  } 

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    
    // stemming + stop word removal
    TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
    TokenizerFactory f2 = new LowerCaseTokenizerFactory(f1);
    //TokenizerFactory f3 = new PorterStemmerTokenizerFactory(f2);
    TokenizerFactory f4 = new EnglishStopTokenizerFactory(f2);
    TokenizerFactory f5 = new RegExFilteredTokenizerFactory(f4, Pattern.compile("[a-z0-9]+"));
    Tokenizer nowTokenizer = f5.tokenizer(question.toCharArray(), 0, question.length());
    String[] words = nowTokenizer.tokenize();
    String[] commonWords = {"what","how","do"};
    TreeSet<String> ts = new TreeSet<String>(Arrays.asList(words));
    ts.removeAll(Arrays.asList(commonWords));
    words = (String[])ts.toArray(new String[0]);     
    
    List<Keyterm> keytermList = new ArrayList<Keyterm>();
    String s = new String("http://swissvar.expasy.org/cgi-bin/swissvar/result?format=xml&global_textfield=");
    URL u;
    XMLParser x = new XMLParser();
    for (String word : words) {
      String keyterm = word;
      //System.out.println(keyterm);
      /*
      try {
        u = new URL(s + keyterm);
       
        List<Node> l = x.getVariants(x.parse(u));
        if(l != null){
          l = l.subList(0, Math.min(2, l.size()));
          for(Node o:l){
              String v = o.getText();
              //System.out.println(v);
              keytermList.add(new Keyterm(v.substring(2,v.length())));
            }
        }
       

      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        //e.printStackTrace();
      } catch (DocumentException e) {
        // TODO Auto-generated catch block
        //e.printStackTrace();
      }
       */
      keytermList.add(new Keyterm(keyterm));
    }
     
    return keytermList;
  }
}
