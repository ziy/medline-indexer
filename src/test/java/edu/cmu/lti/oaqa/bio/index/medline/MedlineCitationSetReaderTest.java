package edu.cmu.lti.oaqa.bio.index.medline;

import com.google.common.collect.Sets;
import org.jdom2.JDOMException;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MedlineCitationSetReaderTest {

  @Test
  public void test() throws JDOMException, IOException {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(getClass().getResourceAsStream(
            "/medline/xml/medsamp2014.xml"));
    Set<Integer> pmids = Sets.newHashSet();
    while (reader.hasNext()) {
      MedlineCitation citation = reader.next();
      pmids.add(citation.getPmid());
    }
    assertTrue(pmids.contains(10612833));
    assertEquals(pmids.size(), 161);
  }

}
