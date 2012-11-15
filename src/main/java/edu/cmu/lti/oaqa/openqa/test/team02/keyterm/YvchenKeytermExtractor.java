package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;

import hw1.kmandavi.CSVToHashMap;
import hw1.kmandavi.GeneTag;
import hw1.kmandavi.PosTagNamedEntityRecognizer;

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
      keytermList.add(new Keyterm(word));
      System.out.println("********************  " + word);
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
