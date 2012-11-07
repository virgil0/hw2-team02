package edu.cmu.lti.oaqa.openqa.test.team02;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

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

    Chunking chunking = chunker.chunk(question);
    for (Chunk chunk : chunking.chunkSet()) {
      String keyterm = question.substring(chunk.start(), chunk.end());
      keytermList.add(new Keyterm(keyterm));
    }

    return keytermList;
  }
}
