package edu.cmu.cs.ziy;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

public class MedlineCitationSetReaderTest {

  @Test
  public void test() throws SAXException, IOException {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(getClass().getResourceAsStream(
            "/medsamp2014.xml"));
    Set<Integer> pmids = Sets.newHashSet();
    while (reader.hasNext()) {
      MedlineCitation citation = reader.next();
      // System.out.println(citation.getPmid());
      pmids.add(citation.getPmid());
      // System.out.println(citation.getArticleTitle());
      // System.out.println(citation.getAbstractText());
    }
    System.out.println(pmids.contains(10612833));
    assertEquals(pmids.size(), 161);
  }

}
