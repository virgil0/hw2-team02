package edu.cmu.lti.oaqa.openqa.test.team02;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStreamReader;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;
import org.htmlparser.util.NodeList;  

public class YucongyKeytermExtractor extends AbstractKeytermExtractor {

  public static final String PARAM_MODELFILE = "model_file";

  private Chunker chunker;
  public static String getWebCon(String domain) throws IOException {

    System.out.println("START READING FROM...("+domain+")");

    StringBuilder sb = new StringBuilder();

    try {

      java.net.URL url = new java.net.URL(domain);

      BufferedReader in = new BufferedReader(new InputStreamReader(url

          .openStream()));

      String line;

      while (in.ready()) {

        line = in.readLine();
        sb.append(line);

      }

      in.close();

    } catch (FileNotFoundException e) { 

      sb.append(e.toString());

      System.err.println("cannot find Synonyms so continue");       

    }
    return sb.toString();

  }
  

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
  protected List<Keyterm> getKeyterms(String question){ 
    /********/
      final String s = "http://words.bighugelabs.com/";
      String c = null;    
      try {
      /***********/
          List<Keyterm> keytermList = new ArrayList<Keyterm>();
          // stemming + stop word removal
          TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
          TokenizerFactory f2 = new LowerCaseTokenizerFactory(f1);
          TokenizerFactory f3 = new PorterStemmerTokenizerFactory(f2);
          TokenizerFactory f4 = new EnglishStopTokenizerFactory(f3);
          TokenizerFactory f5 = new RegExFilteredTokenizerFactory(f4, Pattern.compile("[a-z0-9]+"));
          Tokenizer nowTokenizer = f5.tokenizer(question.toCharArray(), 0, question.length());
          String[] words = nowTokenizer.tokenize();
          for (String word : words) {
              try {
                c = getWebCon(s+word).toString();
              } catch (IOException e) {
                e.printStackTrace();
              }
              Parser parser = Parser.createParser(c, "GBK");         
              HtmlPage page = new HtmlPage(parser);                              
              TagNameFilter filter=  new TagNameFilter("li");
              org.htmlparser.Node node = null;  
              NodeList nodeList = parser.extractAllNodesThatMatch(filter);
              for (int i = 0; i < nodeList.size(); ++i) 
              {  
                  node = nodeList.elementAt(i);
                  if(!node.getParent().getPreviousSibling().getChildren().asString().equals("noun"))
                  {                     
                      continue;
                  }
                  else
                    {
                        keytermList.add(new Keyterm(node.getChildren().asString())); 
                        System.out.println("!!!!!!!!!!!!!! " + node.getChildren().asString());
                    }
               }
              keytermList.add(new Keyterm(word));
              System.out.println("******************** " + word);
          } 
          
          return keytermList;
      }catch (ParserException e) {
        e.printStackTrace();
        return null;
      }

    
}



}
