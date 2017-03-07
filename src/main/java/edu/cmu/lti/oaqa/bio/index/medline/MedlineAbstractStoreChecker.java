package edu.cmu.lti.oaqa.bio.index.medline;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class MedlineAbstractStoreChecker {

  private static final String TABLE_NAME = "pmid2abstract";

  private static final String SELECT_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE pmid in (%s);";

  public static final String PMID_FIELD = "pmid";
  
  public static final String DATE_CREATED_FIELD = "dateCreated";//added by LR 2/1/17
  public static final String DATE_PUBLISHED_FIELD = "dateCreated";//added by LR 2/9/17
  public static final String ABSTRACT_TEXT_FIELD = "abstractText";

  private final Connection connection;

  public MedlineAbstractStoreChecker(String storePath)
          throws IOException, ClassNotFoundException, SQLException {
    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:" + storePath);
    connection.setAutoCommit(false);
  }

  public boolean checkDocs(File file) throws JDOMException, IOException, SQLException {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(file);
    Map<Integer, String> fileMap = new HashMap<Integer, String>();
    while (reader.hasNext()) {
      MedlineCitation citation = reader.next();
      String abstractText = citation.getAbstractText();
      if (abstractText == null || abstractText.trim().isEmpty()) {
        continue;
      }
      fileMap.put(citation.getPmid(),abstractText);
    }
    Statement statement = connection.createStatement();
    String sql = String.format(SELECT_SQL, Joiner.on(',').join(fileMap.keySet()));
    ResultSet results = statement.executeQuery(sql);
    Map<Integer, String> storeMap = new HashMap<Integer, String>();
    while (results.next()) {
      storeMap.put(results.getInt(1), results.getString(2));
    }
    statement.close();
    return fileMap.equals(storeMap);
  }

  public static void main(String[] args)
          throws JDOMException, IOException, SQLException, ClassNotFoundException {
    MedlineAbstractStoreChecker masb = new MedlineAbstractStoreChecker(args[0]);
    File dir = new File(args[1]);
    List<File> files = Lists.newArrayList(dir.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".xml.gz") || name.endsWith(".xml");
      }

    }));
    Collections.sort(files);
    Stopwatch stopwatch = Stopwatch.createStarted();
    Set<File> failedFiles = new HashSet<File>();
    for (File file : files) {
      System.out.print("Checking " + file.getName() + "... ");
      stopwatch.reset();
      stopwatch.start();
      boolean result = masb.checkDocs(file);
      if (!result) {
        failedFiles.add(file);
      }
      System.out.println(result + " " + stopwatch);
    }
    System.out.println("Failed files: " + failedFiles);
  }

}
