package edu.cmu.lti.oaqa.bio.index.medline;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MedlineCitationSetReader implements Iterator<MedlineCitation> {

  private static final String PUBMED_ARTICLE_ELEMENT = "PubmedArticle"; // added by LR 2/9/17 for med17 xml files
  
  private static final String MEDLINE_CITATION_ELEMENT = "MedlineCitation";

  private static final String PMID_ELEMENT = "PMID";

  private static final String ARTICLE_ELEMENT = "Article";
  
  private static final String ARTICLE_JRNAL_ELEMENT = "Journal"; //added by LR 2/9/17
  private static final String ARTICLE_JRNAL_ISS_ELEMENT = "JournalIssue"; //added by LR 2/9/17
  
  private static final String ARTICLE_TITLE_ELEMENT = "ArticleTitle";

  private static final String ABSTRACT_ELEMENT = "Abstract";

  private static final String ABSTRACT_TEXT_ELEMENT = "AbstractText";

  private static final String Date_Created_ELEMENT = "DateCreated";     //added by LR 2/1/17
    
  private static final String Year_Created_ELEMENT = "Year";     //added by LR 2/2/17
  private static final String Month_Created_ELEMENT = "Month";     //added by LR 2/2/17
  private static final String Day_Created_ELEMENT = "Day";     //added by LR 2/2/17
  
  private static final String Date_Published_ELEMENT = "PubDate";     //added by LR 2/9/17
  private static final String Year_Published_ELEMENT = "Year";     //added by LR 2/9/17
  private static final String Year_MedDate_Published_ELEMENT = "MedlineDate";     //added by LR 2/9/17
  //private static final String Month_Published_ELEMENT = "Month";     //added by LR 2/9/17
  
  
  private static SAXBuilder builder = new SAXBuilder();

  private List<Element> citations;
 

  public MedlineCitationSetReader(File file) throws IOException, JDOMException {
    String extName = Files.getFileExtension(file.getName());
    InputStream inputStream;
    if (extName.equals("xml")) {
      inputStream = new BufferedInputStream(new FileInputStream(file));
    } else if (extName.equals("gz")) {
      inputStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)));
    } else {
      throw new IOException("Unsupported file format.");
    }
    Document document = builder.build(inputStream);
    Element rootNode = document.getRootElement();
    citations = rootNode.getChildren(PUBMED_ARTICLE_ELEMENT);
  }

  public MedlineCitationSetReader(InputStream inputStream) throws JDOMException, IOException {
    Document document = builder.build(inputStream);
    Element rootNode = document.getRootElement();
    citations = rootNode.getChildren(PUBMED_ARTICLE_ELEMENT);
  }

  private int idx = 0;

  @Override
  public boolean hasNext() {
    return idx < citations.size();
  }

  @Override
  public MedlineCitation next() {
    
    Element pubmedArticleElement = citations.get(idx++);
    Element citationElement = pubmedArticleElement.getChild(MEDLINE_CITATION_ELEMENT);
    // pmid
    int pmid = Integer.parseInt(citationElement.getChildText(PMID_ELEMENT));
    Element articleElement = citationElement.getChild(ARTICLE_ELEMENT);
    Element journalElement = articleElement.getChild(ARTICLE_JRNAL_ELEMENT);//added by LR 2/9/17
    Element journalIssueElement = journalElement.getChild(ARTICLE_JRNAL_ISS_ELEMENT);//added by LR 2/9/17
    
    // article datecreated //added by LR 2/1/17 and updated 2/2/17
    Element dateCreatedElement = citationElement.getChild(Date_Created_ELEMENT);
    String dateCreated = (dateCreatedElement.getChildText(Year_Created_ELEMENT))+"-"+ (dateCreatedElement.getChildText(Month_Created_ELEMENT))+"-"+ (dateCreatedElement.getChildText(Day_Created_ELEMENT));
    
    // article datecPublished //added by LR 2/9/17 and updated 2/2/17
    Element datePubElement = journalIssueElement.getChild(Date_Published_ELEMENT);
    String datePublished = (datePubElement.getChildText(Year_Published_ELEMENT));
    if (datePublished == null)
        { datePublished = (datePubElement.getChildText(Year_MedDate_Published_ELEMENT));}
               
    
    // article title
    String articleTitle = articleElement.getChildText(ARTICLE_TITLE_ELEMENT);
    // abstract text
    Element abstractElement = articleElement.getChild(ABSTRACT_ELEMENT);
    if (abstractElement == null) {
      return new MedlineCitation(pmid, articleTitle, "", dateCreated, datePublished);
    }
    List<String> abstractTexts = getChildrenTexts(abstractElement, ABSTRACT_TEXT_ELEMENT);
    String abstractText = Joiner.on('\n').join(abstractTexts);
    return new MedlineCitation(pmid, articleTitle, abstractText, dateCreated,datePublished);
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
