package edu.udel.irl.atlas;

import static org.junit.Assert.assertTrue;

import edu.udel.irl.atlas.analysis.AtlasAnalyzer;
import edu.udel.irl.atlas.search.AtlasQuery;
import edu.udel.irl.atlas.search.AtlasQueryParser;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import edu.udel.irl.atlas.util.UPOSMapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.payloads.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.SpanOrTermsBuilder;
import org.apache.lucene.queryparser.xml.builders.SpanQueryBuilder;
import org.apache.lucene.queryparser.xml.builders.SpanQueryBuilderFactory;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    final Path IndexDir = new File("/home/mike/Documents/Index/test").toPath();
    final Directory directory = new MMapDirectory(IndexDir);

    public AppTest() throws IOException {
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    private void createTestIndex() throws IOException {
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new AtlasAnalyzer()));

        Document document1 = new Document();
        Document document2 = new Document();
        document1.add(new TextField("text", "The quick brown fox runs over the crimson dog.", Field.Store.YES));
//        document2.add(new TextField("text", "A web spider waves on the white wall.", Field.Store.YES));
        document2.add(new TextField("text", "The swift red fox walks over the ruby dog.", Field.Store.YES));
        writer.addDocument(document1);
        writer.addDocument(document2);
        writer.close();
    }

    @Test
    public void mainTest() throws IOException, ParseException {
//        this.createTestIndex();
//        System.out.println("Done!!");
//        SpanOrTermsBuilder
        IndexReader reader = DirectoryReader.open(directory);
        AtlasQueryParser queryParser = new AtlasQueryParser("text", new AtlasAnalyzer(), reader);
        SpanQuery query = queryParser.parse("dog");
//
//        SpanOrQuery query = new SpanOrQuery(new SpanTermQuery(new Term("text", "02118333-N")),
//                new SpanTermQuery(new Term("text", "02084071-N")),
//                new SpanTermQuery(new Term("text", "02118333-N")),
//                new SpanTermQuery(new Term("text", "00294579-A")));
//        PayloadScoreQuery payloadScoreQuery = new PayloadScoreQuery(query, new SumPayloadFunction(), false);
//        SpanOrQuery query = new SpanOrQuery(new SpanTermQuery(new Term("text", "fox")),
//                new SpanTermQuery(new Term("text", "dog")),
//                new SpanTermQuery(new Term("text", "00332332-A")));
//        QueryParser parser = new QueryParser("text", new AtlasAnalyzer());
//        Query query = parser.parse("indolent");

        IndexSearcher searcher =  new IndexSearcher(reader);
//        TopDocs docs = searcher.search(payloadScoreQuery, 10);
        TopDocs docs = searcher.search(query, 10);

        ScoreDoc[] hits = docs.scoreDocs;


        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("text"));
        }
        for(ScoreDoc match: hits){
            System.out.println(match.score);
            Explanation explanation = searcher.explain(query, match.doc);
            System.out.println(explanation.toString());
        }
    }

    @Test
    public void UposerTest() throws IOException {
//        UPOSMapper mapper = new UPOSMapper(AtlasConfiguration.getInstance().getPOSMapperFolder()+ "/" + AtlasConfiguration.getInstance().getPOSMapperFile());
        System.out.println(new File(AtlasConfiguration.getInstance().getPOSMapperFolder(),
                AtlasConfiguration.getInstance().getPOSMapperFile()).getCanonicalPath());
//        UPOSMapper mapper = new UPOSMapper(new File(AtlasConfiguration.getInstance().getPOSMapperFolder(),
//                AtlasConfiguration.getInstance().getPOSMapperFile()));
    }
}
