package edu.cmu.lti.oaqa.bio.index.medline;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.junit.Test;

import edu.cmu.lti.oaqa.bio.index.medline.MedlineCitationIndexer;

/**
 * Maven execution: <code>
 * mvn exec:exec -Dindex.dir=src/test/resources/index/lucene -Ddocs.dir=src/test/resources/medline/gz
 * </code>
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class MedlineCitationIndexerTest {

  @Test
  public void test() throws JDOMException, IOException {
    String[] args = { "src/test/resources/index/lucene", "src/test/resources/medline/gz" };
    MedlineCitationIndexer.main(args);
  }

}
