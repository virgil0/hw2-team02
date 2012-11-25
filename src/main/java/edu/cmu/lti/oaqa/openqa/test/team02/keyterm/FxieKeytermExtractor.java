package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 *
 * @author Fei Xie
 */

public class FxieKeytermExtractor extends AbstractKeytermExtractor {

  public static final String PARAM_MODELFILE = "model_file";

  private Chunker chunker;

  public static String getWebCon(String domain) throws IOException {

    System.out.println("START READING FROM...("+domain+")");

    StringBuilder sb = new StringBuilder();

    try {

      java.net.URL url = new java.net.URL(domain);

      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

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
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> keytermList = new ArrayList<Keyterm>();
    final String s = "http://words.bighugelabs.com/";
    String c = null;
    Chunking chunking = chunker.chunk(question);
    for (Chunk chunk : chunking.chunkSet()) {
      String keyterm = question.substring(chunk.start(), chunk.end());
      String nowkeyterm = null;
      keytermList.add(new Keyterm(keyterm));

      Matcher m = Pattern.compile("[A-Z0-9]+").matcher(keyterm);
      if (m.matches() == false){
        System.out.println("*****Find Synonym**********" + keyterm);
        try {
          c = getWebCon(s + keyterm).toString();
        } catch (IOException e) {
          e.printStackTrace();
        }
        Parser parser = Parser.createParser(c, "GBK");         
        HtmlPage page = new HtmlPage(parser);                              
        TagNameFilter filter=  new TagNameFilter("li");
        org.htmlparser.Node node = null;  
        NodeList nodeList = null;
        try {
          nodeList = parser.extractAllNodesThatMatch(filter);
        } catch (ParserException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        int limit = nodeList.size();
        if(nodeList.size() > 5)
          limit = 5;
        for (int i = 0; i < limit; ++i) 
        {  
            node = nodeList.elementAt(i);
            if(!node.getParent().getPreviousSibling().getChildren().asString().equals("noun"))
            {                     
                continue;
            }
            else
              {
                String synonym = node.getChildren().asString();
                Chunking nowchunking = chunker.chunk(synonym);
                System.out.println("!!!Synonym!!!!!!!!!!! " + synonym);
                keytermList.add(new Keyterm(synonym));
/*                for (Chunk nowchunk : nowchunking.chunkSet()) {
                  nowkeyterm = synonym.substring(nowchunk.start(), nowchunk.end());
                  keytermList.add(new Keyterm(nowkeyterm));
                  System.out.println("!!!New Synonym!!!!!!! " + nowkeyterm);
                }*/              
              }
         }
      }
    }

    return keytermList;
  }
}
