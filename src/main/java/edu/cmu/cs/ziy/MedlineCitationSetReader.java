package edu.cmu.cs.ziy;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class MedlineCitationSetReader implements Iterator<MedlineCitation> {

  private static final String MEDLINE_CITATION_ELEMENT = "MedlineCitation";

  private static final String PMID_ELEMENT = "PMID";

  private static final String ARTICLE_TITLE_ELEMENT = "ArticleTitle";

  private static final String ABSTRACT_TEXT_ELEMENT = "AbstractText";

  private static XMLInputFactory factory = XMLInputFactory.newInstance();

  private XMLStreamReader reader;

  public MedlineCitationSetReader(InputStream inputStream) throws XMLStreamException {
    reader = factory.createXMLStreamReader(inputStream);
  }

  @Override
  public boolean hasNext() {
    try {
      while (reader.hasNext()) {
        int event = reader.next();
        if (event == XMLStreamConstants.START_ELEMENT
                && reader.getLocalName().equals(MEDLINE_CITATION_ELEMENT)) {
          return true;
        }
      }
    } catch (XMLStreamException e) {
      return false;
    }
    return false;
  }

  @Override
  public MedlineCitation next() {
    MedlineCitation ret = new MedlineCitation();
    try {
      while (reader.hasNext()) {
        int event = reader.next();
        if (event == XMLStreamConstants.END_ELEMENT
                && reader.getLocalName().equals(MEDLINE_CITATION_ELEMENT)) {
          System.out.println("store " + ret.getPmid());
          return ret;
        }
        if (event != XMLStreamConstants.START_ELEMENT) {
          continue;
        }
        String localName = reader.getLocalName();
        if (localName.equals(PMID_ELEMENT)) {
          reader.next();
          ret.setPmid(Integer.parseInt(reader.getText().trim()));
          System.out.println("set " + ret.getPmid());
        } else if (localName.equals(ARTICLE_TITLE_ELEMENT)) {
          reader.next();
          ret.setArticleTitle(reader.getText().trim());
        } else if (localName.equals(ABSTRACT_TEXT_ELEMENT)) {
          reader.next();
          ret.setAbstractText(reader.getText().trim());
        } else {
          continue;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    throw new RuntimeException();
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub

  }

}
