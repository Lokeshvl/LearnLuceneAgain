# Lucene Index + Search (Maven)

Small Java example that reads a text file, builds a Lucene index in memory, and shows how a query is analyzed and searched.

## Requirements

- Java 17
- Maven 3.8+

## Run

```bash
mvn -q -DskipTests compile exec:java -Dexec.args="data/sample.txt lucene"
```

To try the Gandhi article (downloaded into test resources):

```bash
mvn -q -DskipTests compile exec:java -Dexec.args="src/test/resources/gandhi.txt satyagraha"
```

## What it does

- Reads the file into a single Lucene document.
- Uses `StandardAnalyzer` for both indexing and searching.
- Prints analyzer tokens and the parsed Lucene query.
- Executes the search and shows matching documents with a preview.
- Skips re-indexing if the file has the same size and last-modified timestamp.

## Notes

- The index is stored on disk under `index/` in the project directory.
- Sample text lives in `data/sample.txt`.
- The Gandhi file in `src/test/resources/gandhi.txt` is Wikipedia wikitext (raw article markup).
