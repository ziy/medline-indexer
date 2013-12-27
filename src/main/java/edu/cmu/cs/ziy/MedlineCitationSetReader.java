package edu.cmu.cs.ziy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MedlineCitationSetReader implements Iterator<MedlineCitation> {

  private static final String MEDLINE_CITATION_ELEMENT = "MedlineCitation";

  private static final String PMID_ELEMENT = "PMID";

  private static final String ARTICLE_TITLE_ELEMENT = "ArticleTitle";

  private static final String ABSTRACT_TEXT_ELEMENT = "AbstractText";

  private static DocumentBuilder docBuilder;

  static {
    try {
      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  private NodeList citations;

  public MedlineCitationSetReader(InputStream inputStream) throws SAXException, IOException {
    Document doc = docBuilder.parse(inputStream);
    citations = doc.getChildNodes();
  }

  private int idx = 0;

  @Override
  public boolean hasNext() {
    return idx < citations.getLength();
  }

  @Override
  public MedlineCitation next() {
    Element e = (Element) citations.item(idx++);
    // pmid
    NodeList pmidNodeList = e.getElementsByTagName(PMID_ELEMENT);
    if (pmidNodeList.getLength() == 2) 
    assert pmidNodeList.getLength() == 1;
    int pmid = Integer.parseInt(pmidNodeList.item(0).getTextContent());
    // article title
    NodeList articleTitleNodeList = e.getElementsByTagName(ARTICLE_TITLE_ELEMENT);
    String articleTitle = null;
    if (articleTitleNodeList.getLength() == 1) {
      articleTitle = articleTitleNodeList.item(0).getTextContent();
    }
    // abstract text
    NodeList abstractTextNodeList = e.getElementsByTagName(ABSTRACT_TEXT_ELEMENT);
    String abstractText = null;
    if (abstractTextNodeList.getLength() == 1) {
      abstractText = abstractTextNodeList.item(0).getTextContent();
    }
    return new MedlineCitation(pmid, articleTitle, abstractText);
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub

  }

}
