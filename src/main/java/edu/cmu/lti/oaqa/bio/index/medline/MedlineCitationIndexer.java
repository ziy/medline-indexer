package edu.cmu.lti.oaqa.bio.index.medline;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.List;

public class MedlineCitationIndexer {

  private final IndexWriter writer;

  public static final String PMID_FIELD = "pmid";

  public static final String ARTICLE_TITLE_FIELD = "articleTitle";

  public static final String ABSTRACT_TEXT_FIELD = "abstractText";

  public MedlineCitationIndexer(String indexPath) throws IOException {
    File indexDir = new File(indexPath);
    if (indexDir.exists() && indexDir.listFiles().length > 0) {
      throw new RuntimeException(new FileAlreadyExistsException(indexDir.getAbsolutePath()));
    } else if (!indexDir.exists()) {
      indexDir.mkdir();
    }
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    iwc.setOpenMode(OpenMode.CREATE);
    iwc.setRAMBufferSizeMB(2000);
    writer = new IndexWriter(FSDirectory.open(indexDir.toPath()), iwc);
  }

  public void indexDocs(File file) throws JDOMException, IOException {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(file);
    while (reader.hasNext()) {
      MedlineCitation citation = reader.next();
      Document doc = new Document();
      doc.add(new IntField(PMID_FIELD, citation.getPmid(), Field.Store.YES));
      doc.add(new TextField(ARTICLE_TITLE_FIELD, citation.getArticleTitle(), Field.Store.YES));
      doc.add(new TextField(ABSTRACT_TEXT_FIELD, citation.getAbstractText(), Field.Store.YES));
      writer.addDocument(doc);
    }
    writer.commit();
  }

  public void optimize() throws IOException {
    writer.forceMerge(1);
    writer.close();
  }

  public static void main(String[] args) throws JDOMException, IOException {
    MedlineCitationIndexer mci = new MedlineCitationIndexer(args[0]);
    File dir = new File(args[1]);
    List<File> files = Lists.newArrayList(dir.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".xml.gz") || name.endsWith(".xml");
        }

    }));
    Collections.sort(files);
    Stopwatch stopwatch = Stopwatch.createStarted();
    for (File file : files) {
      System.out.print("Indexing " + file.getName() + "... ");
      stopwatch.reset();
      stopwatch.start();
      mci.indexDocs(file);
      System.out.println(stopwatch);
    }
    System.out.print("Optimizing... ");
    stopwatch.reset();
    stopwatch.start();
    mci.optimize();
    System.out.println(stopwatch);
  }

}
