Minimal Medline Indexer
=======================

This project provides (a) a minimal Lucene index builder and (b) a minimal SQLite database builder for [Medline (abstract collection for PubMed articles)](https://mbr.nlm.nih.gov/Download/).

The Lucene index has three fields: `pmid`, `abstractText`, and `articleTitle` with the latter two searchable, and the SQLite database has two fields: `pmid` and `abstract` with the an index built on `pmid`.

This project is used in the preparation of the [OAQA BioASQ System](https://github.com/oaqa/bioasq).


Use it
------

Two use cases have been configured as `exec:exec` goals in the [`pom.xml`](pom.xml) file.

__Build Lucene index__

```
mvn -Ddocs.dir=DOCS_DIR -Dindex.dir=INDEX_DIR exec:exec@index
```

where `DOCS_DIR` is the _input_ directory that contains the downloaded `.xml.gz` or `.xml` files and `INDEX_DIR` is the _output_ directory for the Lucene index. 

__Build SQLite database__

```
mvn -Ddocs.dir=DOCS_DIR -Ddb.path=DB_PATH exec:exec@store
```

where `DOCS_DIR` is the _input_ directory that contains the downloaded `.xml.gz` or `.xml` files and `DB_PATH` is the _output_ SQLite database file path.