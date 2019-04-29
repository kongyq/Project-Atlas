package edu.udel.irl.atlas.benchmark;

import edu.udel.irl.atlas.analysis.AtlasAnalyzer;
import edu.udel.irl.atlas.search.AtlasQueryParser;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Trec7Search {

    public static void main(String[] args) throws IOException {

        final Path IndexDir = new File("/home/mike/Documents/Index/Trec7").toPath();
        final Directory directory = new NIOFSDirectory(IndexDir);

        long startTime = System.currentTimeMillis();

        String queryText = "Falkland petroleum exploration";

        IndexReader reader = DirectoryReader.open(directory);
        AtlasQueryParser queryParser = new AtlasQueryParser("text", new AtlasAnalyzer(), reader);
        Query query = queryParser.parse(queryText);

        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, 1000);
        ScoreDoc[] hits = docs.scoreDocs;

        System.out.println("Found " + hits.length + " hits.");
        int i = 1;
        for(ScoreDoc match: hits){
            float score = match.score;
            String docId = searcher.doc(match.doc).get("docId");
            System.out.println(i + " : " + score + " : " + docId);
            i++;
        }
        reader.close();

        long endTime = System.currentTimeMillis();
        System.out.println("Search query : " + queryText +" : took " + (endTime - startTime) / 1000 + " seconds.");
    }
}
