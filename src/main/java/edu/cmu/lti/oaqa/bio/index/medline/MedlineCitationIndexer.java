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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;

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
  
  public static final String DATE_CREATED_FIELD = "dateCreated"; //created by LR 2/1/17
  public static final String DATE_PUB_FIELD = "datePublished"; //created by LR 2/9/17
  
  public static String dirYear = "0001"; //created by LR 2/5/17
  
  public MedlineCitationIndexer(String indexPath) throws IOException {
    
    dirYear = indexPath.substring(indexPath.length()-4);
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

  public void indexDocs(File file) throws JDOMException, IOException 
  {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(file);
    while (reader.hasNext()) {
      MedlineCitation citation = reader.next();
      //String dirYear = citation.getDateCreated().substring(0,5);
      if(citation.getDatePublished().startsWith(dirYear))
      {
      Document doc = new Document();
     
      
    //Added by TC 2/2/17  
    //create new FieldType to store term positions (TextField is not sufficiently configurable)
    FieldType ft = new FieldType();
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    ft.setTokenized(true);
    ft.setStoreTermVectors(true);
    ft.setStoreTermVectorPositions(true);
    Field contentsField = new Field(ABSTRACT_TEXT_FIELD, citation.getAbstractText(), ft);
    doc.add(contentsField);
      
      doc.add(new IntField(PMID_FIELD, citation.getPmid(), Field.Store.YES));
      doc.add(new TextField(ARTICLE_TITLE_FIELD, citation.getArticleTitle(), Field.Store.YES));
     // doc.add(new TextField(ABSTRACT_TEXT_FIELD, citation.getAbstractText(), Field.Store.YES));
      doc.add(new TextField(DATE_CREATED_FIELD, citation.getDateCreated(), Field.Store.YES));//created by LR 2/1/17
      doc.add(new TextField(DATE_PUB_FIELD, citation.getDatePublished(), Field.Store.YES));//created by LR 2/1/17
      writer.addDocument(doc);
      }
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
