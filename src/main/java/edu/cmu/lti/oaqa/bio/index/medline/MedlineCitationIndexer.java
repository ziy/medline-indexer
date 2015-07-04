package edu.cmu.lti.oaqa.bio.index.medline;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class MedlineCitationIndexer {

  private static class SuppportedExtensionFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
      for (String extension : supportedExtensions) {
        if (name.endsWith(extension)) {
          return true;
        }
      }
      return false;
    }
  }

  private IndexWriter writer;

  public static String PMID_FIELD = "pmid";

  public static String ARTICLE_TITLE_FIELD = "articleTitle";

  public static String ABSTRACT_TEXT_FIELD = "abstractText";

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

  public static Set<String> supportedExtensions = Sets.newHashSet(".xml.gz", ".xml");

  public void indexDocs(File file) throws JDOMException, IOException {
    String extName = Files.getFileExtension(file.getName());
    if (extName.equals("xml")) {
      indexDocs(new BufferedInputStream(new FileInputStream(file)));
    } else if (extName.equals("gz")) {
      indexDocs(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
    }
  }

  public void indexDocs(InputStream inputStream) throws JDOMException, IOException {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(inputStream);
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
    List<File> files = Lists.newArrayList(dir.listFiles(new SuppportedExtensionFilter()));
    Collections.sort(files);
    Stopwatch stopwatch = Stopwatch.createStarted();
    for (File file : files) {
      System.out.print("Indexing " + file.getName() + "... ");
      stopwatch.reset();
      mci.indexDocs(file);
      System.out.println(stopwatch.elapsed(TimeUnit.SECONDS) + " secs");
    }
    System.out.print("Optimizing... ");
    stopwatch.reset();
    mci.optimize();
    System.out.println(stopwatch.elapsed(TimeUnit.SECONDS) + " secs");
  }
}
