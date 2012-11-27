package edu.cmu.lti.oaqa.openqa.test.team02.utilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class XMLParser {
  
  public static void main(String[] args) throws MalformedURLException, DocumentException{
    URL u = new URL("http://swissvar.expasy.org/cgi-bin/swissvar/result?format=xml&global_textfield=bard1");
    
    Document d = parse(u);
    getVariants(d);
    
  }
  

  public static Document parse(URL url) throws DocumentException {
    SAXReader reader = new SAXReader();
    Document document = reader.read(url);
    return document;
  }
  
  public static List getVariants(Document document) throws DocumentException{
    Element root = document.getRootElement();
    List l = document.selectNodes("table/entries/entry/diseases/disease/variants/variant/variant_name");
    List l2 = document.selectNodes("table/entries/entry/diseases/variants/variant/variant_name");
    if( l != null){
      if(l2 != null ){
        l.addAll(l2);
      }
      return l;
    }
    else 
      return null;
    /*
    for(Object o:l){
      System.out.println(((Element) o).getText());
    }
    */

    
  }
  
}