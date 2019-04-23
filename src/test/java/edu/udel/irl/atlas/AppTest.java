package edu.udel.irl.atlas;

import static org.junit.Assert.assertTrue;

import edu.udel.irl.atlas.analysis.AtlasAnalyzer;
import edu.udel.irl.atlas.search.*;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import edu.udel.irl.atlas.util.UPOSMapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
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
import java.util.List;

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

        document1.add(new TextField("text", "FR940706-0-00014 Tables to §393.102( b )( 6 ) _. The quick brown fox runs over the crimson dog.", Field.Store.YES));
        document1.add(new StoredField("docId", "document 1"));

        document2.add(new TextField("text", "2100 ( 950 ) 3/8 ( 10 ) 3000 ( 1360 ) 7/16 ( 11 ) 4100 ( 1860 ) 1/2 ( 13 ) 5300 ( 2400 ) 5/8 ( 16 ) 8300 ( 3770 ) 3/4 ( 20 ) 10900 ( 4940 ) 7/8 ( 22 ) 16100 ( 7300 ) 1 ( 25 ) 20900 ( 9480 ) s ,s 1 Manila Rope WLL 0 s ,s 3/8 ( 10 ) 205 (90 ) 7/16 ( 11 ) 265 ( 120 ) 1/2 ( 13 ) 315 ( 150 ) 5/8 ( 16 ) 465 ( 210 ) 3/4 ( 20 ) 640 ( 290 ) 1 ( 25 ) 1050 ( 480 ) s ,s 1 Polypropylene Fiber Rope WLL (3-Strand and 8-Strand Constructions ) 0 s ,s 3/8 ( 10 ) 400 ( 180 ) 7/16 ( 11 ) 525 ( 240 ) 1/2 ( 13 ) 625 ( 280 ) 5/8 ( 16 ) 925 ( 420 ) 3/4 ( 20 ) 1275 ( 580 ) 1 ( 25 ) 2100 ( 950 ) s ,s 1 Polyester Fiber Rope WLL (3-Strand and 8-Strand Constructions ) 0 s ,s 3/8 ( 10 ) 555 ( 250 ) 7/16 ( 11 ) 750 ( 340 ) 1/2 ( 13 ) 960 ( 440 ) 5/8 ( 16 ) 1500 ( 680 ) 3/4 ( 20 ) 1880 ( 850 ) 1 ( 25 ) 3300 ( 1500 ) s ,s 1 Nylon Rope WLL 0 s ,s 3/8 ( 10 ) 278 ( 130 ) 7/16 ( 11 ) 410 ( 190 ) 1/2 ( 13 ) 525 ( 240 ) 5/8 ( 16 ) 935 ( 420 ) 3/4 ( 20 ) 1420 ( 640 ) 1 ( 25 ) 2520 ( 1140 ) s ,s", Field.Store.YES));
        document2.add(new StoredField("docId", "document 2"));

//        document1.add(new TextField("text", "The quick brown fox runs over the crimson dog. There are red apples on those trees.", Field.Store.YES));
//        document1.add(new TextField("docId", "document 1", Field.Store.YES));
////        document2.add(new TextField("text", "A web spider waves on the white wall.", Field.Store.YES));
//        document2.add(new TextField("text", "The swift red fox walks over the ruby dog. This is a pink peach under the tree.", Field.Store.YES));
//        document2.add(new TextField("docId", "document 2", Field.Store.YES));
//        writer.addDocument(document1);
//        writer.addDocument(document2);
        writer.close();
    }

    @Test
    public void mainTest() throws IOException, ParseException {
        this.createTestIndex();
        System.out.println("Done!!");
//
        System.exit(1);
        IndexReader reader = DirectoryReader.open(directory);
        AtlasQueryParser queryParser = new AtlasQueryParser("text", new AtlasAnalyzer(), reader);
        Query query = queryParser.parse("dog fox");
//        SpanBoostQuery boostQuery = new SpanBoostQuery(query, 2.0f);
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
//
        ScoreDoc[] hits = docs.scoreDocs;

//        AtlasIndexSearcher indexSearcher = new AtlasIndexSearcher(reader);
//
//        AtlasTopDocs topDocs = indexSearcher.search(boostQuery, 10);
//
//        AtlasScoreDoc[] hits = topDocs.scoreDocs;


//        System.out.println("Found " + hits.length + " hits.");
//        for(int i=0;i<hits.length;++i) {
//            String docId = hits[i].docId;
//            float score = hits[i].score;
////            Document d = searcher.doc(docId);
//            System.out.println((i + 1) + ". " + docId + " score: " + score);
//        }
//
//        for(AtlasScoreDoc scoreDoc: hits){
//            Explanation explanation = indexSearcher.explain(query, scoreDoc);
//            System.out.println(explanation.toString());
//        }

//        for(AtlasScoreDoc scoreDoc: hits){
//            for(ScoreDoc match: scoreDoc.scoreDocs){
//                System.out.println(match.score);
//                Explanation explanation = indexSearcher.explain(query, match.doc);
//                System.out.println(explanation.toString());
//            }
//        }

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
        reader.close();
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
