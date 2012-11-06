package edu.cmu.lti.oaqa.openqa.test.team02.keyterm;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class GeneKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    // TODO Auto-generated method stub
    question = question.replace('?', ' ');
    question = question.replace('(', ' ');
    question = question.replace('[', ' ');
    question = question.replace(')', ' ');
    question = question.replace(']', ' ');
    question = question.replace('/', ' ');
    question = question.replace('\'', ' ');

    String[] questionTokens = question.split("\\s+");
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    for (int i = 0; i < questionTokens.length; i++) {
      keyterms.add(new Keyterm(questionTokens[i]));
    }

    return keyterms;
  }

}
