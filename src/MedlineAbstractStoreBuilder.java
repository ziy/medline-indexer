package edu.cmu.lti.oaqa.bio.index.medline;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.jdom2.JDOMException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class MedlineAbstractStoreBuilder {

  private static final String TABLE_NAME = "pmid2abstract";

  private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

  private static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " (" +
                                                       "pmid INTEGER NOT NULL PRIMARY KEY, " +
                                                       "dateCreated, "+ //added by LR 2/1/17
                                                       "datePublished, "+ //added by LR 2/9/17
                                                       "abstract TEXT" +
                                                   ");";

  private static final String INSERT_PREP_SQL = "INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES (?,?,?,?);";

  public static final String PMID_FIELD = "pmid";
  public static final String DATE_CREATED_FIELD = "dateCreated";//added by LR 2/1/17
  public static final String DATE_PUBLISHED_FIELD = "datePublished";//added by LR 2/9/17
  public static final String ABSTRACT_TEXT_FIELD = "abstractText";

  private final Connection connection;

  public MedlineAbstractStoreBuilder(String storePath)
          throws IOException, ClassNotFoundException, SQLException {
    Files.deleteIfExists(Paths.get(storePath));
    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:" + storePath);
    connection.setAutoCommit(false);
    Statement statement = connection.createStatement();
    statement.executeUpdate(DROP_TABLE_SQL);
    statement.executeUpdate(CREATE_TABLE_SQL);
    statement.close();
    connection.commit();
  }

  public StoreDocResult storeDocs(File file) throws JDOMException, IOException, SQLException {
    MedlineCitationSetReader reader = new MedlineCitationSetReader(file);
    PreparedStatement statement = connection.prepareStatement(INSERT_PREP_SQL);
    int addedCount = 0;
    int ignoredCount = 0;
    while (reader.hasNext()) {
      MedlineCitation citation = reader.next();
      String abstractText = citation.getAbstractText();
      if (abstractText == null || abstractText.trim().isEmpty()) {
        ignoredCount++;
        continue;
      }
      statement.setInt(1, citation.getPmid());
      statement.setString(2, citation.getDateCreated());//added by LR 2/1/17
      statement.setString(3, citation.getDatePublished());//added by LR 2/9/17
      statement.setString(4, abstractText);
//      statement.setBytes(2, gzip(abstractText));
      statement.addBatch();
      addedCount++;
    }
    statement.executeBatch();
    statement.close();
    connection.commit();
    return new StoreDocResult(addedCount, ignoredCount);
  }

  private static byte[] gzip(String s) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(bos);
    OutputStreamWriter osw = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
    osw.write(s);
    osw.close();
    return bos.toByteArray();
  }

  public void optimize() throws IOException, SQLException {
    connection.commit();
    connection.close();
  }

  public static void main(String[] args)
          throws JDOMException, IOException, SQLException, ClassNotFoundException {
    MedlineAbstractStoreBuilder masb = new MedlineAbstractStoreBuilder(args[0]);
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
      System.out.print("Storing " + file.getName() + "... ");
      stopwatch.reset();
      stopwatch.start();
      StoreDocResult result = masb.storeDocs(file);
      System.out.println("+" + result.addedCount + "/-" + result.ignoredCount + " " + stopwatch);
    }
    System.out.print("Optimizing... ");
    masb.optimize();
    stopwatch.reset();
    stopwatch.start();
    System.out.println(stopwatch);
  }

  private static class StoreDocResult {

    private int addedCount;

    private int ignoredCount;

    public StoreDocResult(int addedCount, int ignoredCount) {
      this.addedCount = addedCount;
      this.ignoredCount = ignoredCount;
    }

    public int getAddedCount() {
      return addedCount;
    }

    public int getIgnoredCount() {
      return ignoredCount;
    }

  }

}
