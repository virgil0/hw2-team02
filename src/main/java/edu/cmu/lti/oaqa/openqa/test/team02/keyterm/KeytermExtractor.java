package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.dom4j.DocumentException;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.openqa.test.team02.utilities.XMLParser;

/**
 *
 * @author Team 02
 */

public class KeytermExtractor extends AbstractKeytermExtractor {

  public static final String PARAM_NERMODELFILE = "ner_file";
  public static final String PARAM_POSGENMODELFILE = "posgen_file";
  public static final String PARAM_POSMEDMODELFILE = "posmed_file";
  private HmmDecoder genDecoder, medDecoder;
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
    File NERmodelFile = new File(((String) c.getConfigParameterValue(PARAM_NERMODELFILE)).trim());
    File POSgenmodelFile = new File(((String) c.getConfigParameterValue(PARAM_POSGENMODELFILE)).trim());
    File POSmedmodelFile = new File(((String) c.getConfigParameterValue(PARAM_POSMEDMODELFILE)).trim());
    if (!NERmodelFile.exists() || !POSgenmodelFile.exists() || !POSmedmodelFile.exists()) {
      throw new ResourceInitializationException();
    }

    try {
      genDecoder = new HmmDecoder((HiddenMarkovModel) AbstractExternalizable.readObject(POSgenmodelFile));
      medDecoder = new HmmDecoder((HiddenMarkovModel) AbstractExternalizable.readObject(POSmedmodelFile));
      chunker = (Chunker) AbstractExternalizable.readObject(NERmodelFile);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    
    List<Keyterm> keytermList = new ArrayList<Keyterm>();
    String[] tokens = null;
    List<String> tokenList = new ArrayList<String>();    
    
    if(!question.endsWith("?"))
      question = question.concat("?");
    
    // find ()
    String nowQues = new String();
    int preEnd = 0;
    boolean flag = false;
    Matcher key = Pattern.compile("\\([\\w|\\'|\\s|\\d|-]+\\)").matcher(question);
    while(key.find()){
      String insider = question.substring(key.start() + 1, key.end() - 1);
      System.out.println("Extract insider: " + insider);
      nowQues = nowQues.concat(question.substring(preEnd, key.start() - 1));
      keytermList.add(new Keyterm(insider));
      preEnd = key.end();
      flag = true;
    }
    if(flag){
      nowQues = nowQues.concat(question.substring(preEnd, question.length()));
      System.out.println(nowQues);
    }
    else{
      nowQues = question;
    }

      
    /*String s1 = new String("http://words.bighugelabs.com/");
    String s2 = new String("http://swissvar.expasy.org/cgi-bin/swissvar/result?format=xml&global_textfield=");
    URL u;
    XMLParser x = new XMLParser();
    String c = null;*/
    Chunking chunking = chunker.chunk(nowQues);
    Chunk preChunk = null;
    flag = false;
    for (Chunk chunk : chunking.chunkSet()) {
      if(!flag){
        tokens = nowQues.substring(0, chunk.start()).split(" ");
        for(String token: tokens){
          if(token.length() > 0)
            tokenList.add(token);
        }
      }
      else{
        tokens = nowQues.substring(preChunk.end(), chunk.start()).split(" ");
        for(String token: tokens){
          if(token.length() > 0)
            tokenList.add(token);
        }
      }
      flag = true;
      String keyterm = nowQues.substring(chunk.start(), chunk.end());
      tokenList.add(keyterm);
      keytermList.add(new Keyterm(keyterm));
      System.out.println("Bio Keyterm: " + keyterm + " (" + chunk.type() + ")");
      
/*      Matcher m = Pattern.compile("[A-Z0-9]+").matcher(keyterm);
      if (m.matches() == false){
        System.out.println("*****Find Synonym**********" + keyterm);
        try {
          c = getWebCon(s1 + keyterm).toString();
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
        for (int i = 0; i < limit; ++i){  
            node = nodeList.elementAt(i);
            if(node.getParent().getPreviousSibling().getChildren().asString().equals("noun")){
              String synonym = node.getChildren().asString();
              Chunking nowchunking = chunker.chunk(synonym);
              System.out.println("!!!Synonym!!!!!!!!!!! " + synonym);
              //keytermList.add(new Keyterm(synonym));
            }
         }
      }*/
      preChunk = chunk;
    }
    System.out.println(nowQues);
    if(preChunk.end() < nowQues.length() - 1){
      tokens = nowQues.substring(preChunk.end(), nowQues.length() - 1).split(" ");
      for(String token: tokens){
        if(token.length() > 0)
          tokenList.add(token);
      }
    }
    tokenList.add("?");
    
    // find synonym
    String s1 = new String("http://words.bighugelabs.com/");
    String c = null;
    List<String> synonymList = new ArrayList<String>();
    for(Keyterm hitkey : keytermList){
      String keyterm = hitkey.toString();
      System.out.println("~~~~" + keyterm);
      Matcher m = Pattern.compile("[A-Z0-9]+").matcher(keyterm);
      if (m.matches() == false){
        System.out.println("*****Find Synonym**********" + keyterm);
        try {
          c = getWebCon(s1 + keyterm).toString();
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
          e.printStackTrace();
        }
        int limit = nodeList.size();
        if(nodeList.size() > 5)
          limit = 5;
        for (int i = 0; i < limit; ++i){  
          node = nodeList.elementAt(i);
          if(node.getParent().getPreviousSibling().getChildren().asString().equals("noun")){
            String synonym = node.getChildren().asString();
            //Chunking nowchunking = chunker.chunk(synonym);
            System.out.println("!!!Synonym!!!!!!!!!!! " + synonym);
            synonymList.add(synonym);
          }
        }
      }
    } 
    /*for(String syn : synonymList){
      keytermList.add(new Keyterm(syn));
    }*/
    
    // pos tagging
    Tokenizer nowTokenizer;
    Tagging<String> genTagging = genDecoder.tag(tokenList);
    Tagging<String> medTagging = medDecoder.tag(tokenList);
    System.out.println("=============== POS ====================");
    String nowTag, preTag = null;
    for (int i = 0; i < genTagging.size(); i ++){
      nowTag = genTagging.tag(i);
      if((nowTag.startsWith("VB") && !preTag.startsWith("W")) || (nowTag.startsWith("NN"))){
        System.out.print("[" + genTagging.token(i) + "_" + nowTag + "] ");
        //keytermList.add(new Keyterm(genTagging.token(i)));
      }
      else
        System.out.print(genTagging.token(i) + "_" + nowTag + " ");
      preTag = nowTag;
    }
    System.out.println("");
    for (int i = 0; i < medTagging.size(); i ++){
      nowTag = medTagging.tag(i);
      if(nowTag.startsWith("VV") || (nowTag.startsWith("NN"))){
        System.out.print("[" + medTagging.token(i) + "_" + nowTag + "] ");
        //keytermList.add(new Keyterm(medTagging.token(i)));
      }
      else
        System.out.print(medTagging.token(i) + "_" + nowTag + " ");
      preTag = nowTag;
    }
    System.out.println("");
    // original word sequence
    TokenizerFactory f0 = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");  
    nowTokenizer = f0.tokenizer(nowQues.toCharArray(), 0, nowQues.length());
    tokens = nowTokenizer.tokenize();
    genTagging = genDecoder.tag(Arrays.asList(tokens));
    medTagging = medDecoder.tag(Arrays.asList(tokens));
    System.out.println("=============== Ori POS ====================");
    preTag = null;
    for (int i = 0; i < genTagging.size(); i ++){
      nowTag = genTagging.tag(i);
      if((nowTag.startsWith("VB") && !preTag.startsWith("W")) || (nowTag.startsWith("NN"))){
        System.out.print("[" + genTagging.token(i) + "_" + nowTag + "] ");
        //keytermList.add(new Keyterm(genTagging.token(i)));
      }
      else
        System.out.print(genTagging.token(i) + "_" + nowTag + " ");
      preTag = nowTag;
    }
    System.out.println("");
    for (int i = 0; i < medTagging.size(); i ++){
      nowTag = medTagging.tag(i);
      if(nowTag.startsWith("VV") || (nowTag.startsWith("NN"))){
        System.out.print("[" + medTagging.token(i) + "_" + nowTag + "] ");
        keytermList.add(new Keyterm(medTagging.token(i)));
      }
      else
        System.out.print(medTagging.token(i) + "_" + nowTag + " ");
      preTag = nowTag;
    }
    System.out.println("");
    return keytermList;
  }
}
