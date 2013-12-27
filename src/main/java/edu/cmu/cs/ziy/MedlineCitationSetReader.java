package edu.cmu.cs.ziy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class MedlineCitationSetReader implements Iterator<MedlineCitation> {

  private static final String MEDLINE_CITATION_ELEMENT = "MedlineCitation";

  private static final String PMID_ELEMENT = "PMID";

  private static final String ARTICLE_ELEMENT = "Article";

  private static final String ARTICLE_TITLE_ELEMENT = "ArticleTitle";

  private static final String ABSTRACT_ELEMENT = "Abstract";

  private static final String ABSTRACT_TEXT_ELEMENT = "AbstractText";

  private static SAXBuilder builder = new SAXBuilder();

  private List<Element> citations;

  public MedlineCitationSetReader(InputStream inputStream) throws JDOMException, IOException {
    Document document = (Document) builder.build(inputStream);
    Element rootNode = document.getRootElement();
    citations = rootNode.getChildren(MEDLINE_CITATION_ELEMENT);
  }

  private int idx = 0;

  @Override
  public boolean hasNext() {
    return idx < citations.size();
  }

  @Override
  public MedlineCitation next() {
    Element citationElement = (Element) citations.get(idx++);
    // pmid
    int pmid = Integer.parseInt(citationElement.getChildText(PMID_ELEMENT));
    Element articleElement = citationElement.getChild(ARTICLE_ELEMENT);
    // article title
    String articleTitle = articleElement.getChildText(ARTICLE_TITLE_ELEMENT);
    // abstract text
    Element abstractElement = articleElement.getChild(ABSTRACT_ELEMENT);
    if (abstractElement == null) {
      return new MedlineCitation(pmid, articleTitle, null);
    }
    List<String> abstractTexts = getChildrenTexts(abstractElement, ABSTRACT_TEXT_ELEMENT);
    String abstractText = Joiner.on('\n').join(abstractTexts);
    return new MedlineCitation(pmid, articleTitle, abstractText);
  }

  private static List<String> getChildrenTexts(Element element, String cname) {
    List<Element> childrenElements = element.getChildren(cname);
    return Lists.transform(childrenElements, new Function<Element, String>() {

      @Override
      public String apply(Element input) {
        return input.getText();
      }
    });
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub

  }

}
