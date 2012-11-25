package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;

import hw1.kmandavi.CSVToHashMap;
import hw1.kmandavi.GeneTag;
import hw1.kmandavi.PosTagNamedEntityRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class YvchenKeytermExtractor extends AbstractKeytermExtractor {

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
    FileInputStream fileIn;
    HmmDecoder decoder = null;
    try {
      fileIn = new FileInputStream("src/main/resources/mode/pos-en-biogenia.HiddenMarkovModel");
      ObjectInputStream objIn = null;
      try {
        objIn = new ObjectInputStream(fileIn);
        HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
        objIn.close();
        decoder = new HmmDecoder(hmm);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Tokenizer nowTokenizer;
    String[] tokens;
    List<String> tokenList;
    List<Keyterm> keytermList = null;
    
    // stemming + stop word removal
    TokenizerFactory f0 = IndoEuropeanTokenizerFactory.INSTANCE;
    TokenizerFactory f1 = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");
    nowTokenizer = f1.tokenizer(question.toCharArray(), 0, question.length());
    tokens = nowTokenizer.tokenize();
    tokenList = Arrays.asList(tokens);
    Tagging<String> tagging = decoder.tag(tokenList);
    for (int i = 0; i < tagging.size(); ++ i)
      System.out.print(tagging.token(i) + "_" + tagging.tag(i) + " ");
    
    TokenizerFactory f2 = new LowerCaseTokenizerFactory(f0);
    TokenizerFactory f6 = new RegExFilteredTokenizerFactory(f2, Pattern.compile("([a-z0-9 ]+)"));
    nowTokenizer = f6.tokenizer(question.toCharArray(), 0, question.length());
    tokens = nowTokenizer.tokenize();
    for (String token : tokens) {
      keytermList.add(new Keyterm(token));
      System.out.println("===================  " + token);
    }
    
    TokenizerFactory f3 = new PorterStemmerTokenizerFactory(f2);
    TokenizerFactory f4 = new EnglishStopTokenizerFactory(f3);
    TokenizerFactory f5 = new RegExFilteredTokenizerFactory(f4, Pattern.compile("[a-z0-9]+"));
    nowTokenizer = f5.tokenizer(question.toCharArray(), 0, question.length());
    tokens = nowTokenizer.tokenize();
    
    for (String token : tokens) {
      keytermList.add(new Keyterm(token));
      System.out.println("********************  " + token);
    }
    /*
    Chunking chunking = chunker.chunk(question);
    for (Chunk chunk : chunking.chunkSet()) {
      String keyterm = question.substring(chunk.start(), chunk.end());
      keytermList.add(new Keyterm(keyterm));
    }
    */
    return keytermList;
  }
}
