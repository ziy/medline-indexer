package edu.cmu.lti.oaqa.bio.index.medline;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.jdom2.JDOMException;
import org.junit.Test;

/**
 * Maven execution: <code>
 * mvn exec:exec -Dindex.dir=src/test/resources/index/lucene -Ddocs.dir=src/test/resources/medline/gz
 * </code>
 *
 * @author Zi Yang <ziy@cs.cmu.edu>
 */
public class MedlineCitationIndexerTest {

  @Test
  public void test() throws JDOMException, IOException, ParseException {
    File datasetDir = new File("src/test/resources/medline/gz");
    File indexDir = new File("src/test/resources/index/lucene2010");
    // delete index folder if it exists
    if (indexDir.exists()) {
      for (File file : indexDir.listFiles()) {
        file.delete();
      }
    }
    indexDir.delete();
    // index
    MedlineCitationIndexer
            .main(new String[] { indexDir.getAbsolutePath() , datasetDir.getAbsolutePath()
            });
    // search
    
   IndexReader reader = DirectoryReader.open(FSDirectory.open(indexDir.toPath()));
    IndexSearcher searcher = new IndexSearcher(reader);
    QueryParser parser = new MultiFieldQueryParser(new String[] { "articleTitle", "abstractText" },
            new StandardAnalyzer());
    Query query = parser
            .parse(QueryParser
                    .escape("2-(S)-(1-(R)-(3, 5-bis(trifluoromethyl)phenyl)ethoxy)-3-(S)-(4-fluoro)phenyl-4-(5-(2- phosphoryl-3-oxo-4H,-1,2,4-triazolo)methylmorpholine, bis(N-methyl-D-glucamine)"));
    TopDocs results = searcher.search(query, 10);
    System.out.println(results.totalHits);
    for (ScoreDoc scoreDoc : results.scoreDocs) {
      System.out.println(reader.document(scoreDoc.doc).get("pmid"));
    }
  }

}
