package edu.cmu.lti.oaqa.openqa.hellobioqa.retrieval;

import lemurproject.indri.*;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class BioDocumentRetrival   extends AbstractRetrievalStrategist{
  
  protected Integer hitListSize;
  QueryEnvironment env;
  ScoredExtentResult[] results;
  
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      env = new QueryEnvironment();
      env.addServer(serverUrl);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }
  
  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    for (Keyterm keyterm : keyterms) {
      result.append(keyterm.getText() + " ");
    }
    String query = result.toString();
    System.out.println(" QUERY: " + query);
    return query;
  }
  
  @Override
  protected List<RetrievalResult> retrieveDocuments(String arg0, List<Keyterm> keyterms) {
    String query = formulateQuery(keyterms);
    try {
      results = env.runQuery(query, hitListSize);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

}
