package edu.cmu.cs.ziy;

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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + pmid;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MedlineCitation other = (MedlineCitation) obj;
    if (pmid != other.pmid)
      return false;
    return true;
  }

  public int getPmid() {
    return pmid;
  }

  public void setPmid(int pmid) {
    this.pmid = pmid;
  }

  public String getArticleTitle() {
    return articleTitle;
  }

  public void setArticleTitle(String articleTitle) {
    this.articleTitle = articleTitle;
  }

  public String getAbstractText() {
    return abstractText;
  }

  public void setAbstractText(String abstractText) {
    this.abstractText = abstractText;
  }

}
