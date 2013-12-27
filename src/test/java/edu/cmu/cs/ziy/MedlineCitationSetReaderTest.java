package edu.cmu.cs.ziy;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.jdom2.JDOMException;
import org.junit.Test;

import com.google.common.collect.Sets;

public class MedlineCitationSetReaderTest {

  @Test
  public void test() throws JDOMException, IOException {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(getClass().getResourceAsStream(
            "/medsamp2014.xml"));
    Set<Integer> pmids = Sets.newHashSet();
    while (reader.hasNext()) {
      MedlineCitation citation = reader.next();
      pmids.add(citation.getPmid());
    }
    assertTrue(pmids.contains(10612833));
    assertEquals(pmids.size(), 161);
  }

}
