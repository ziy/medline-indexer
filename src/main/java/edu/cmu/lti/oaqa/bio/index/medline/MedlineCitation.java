package edu.cmu.lti.oaqa.bio.index.medline;

import com.google.common.base.Objects;

public class MedlineCitation {

  private int pmid;

  private String articleTitle;

  private String abstractText;

  public MedlineCitation(int pmid, String articleTitle, String abstractText) {
    this.pmid = pmid;
    this.articleTitle = articleTitle;
    this.abstractText = abstractText;
  }

  @Override
  public String toString() {
    return String.valueOf(pmid);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MedlineCitation that = (MedlineCitation) o;
    return Objects.equal(pmid, that.pmid);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(pmid);
  }

  public int getPmid() {
    return pmid;
  }

  public String getArticleTitle() {
    return articleTitle;
  }

  public String getAbstractText() {
    return abstractText;
  }

}
