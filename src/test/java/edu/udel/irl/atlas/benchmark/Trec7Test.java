package edu.udel.irl.atlas.benchmark;

import edu.udel.irl.atlas.analysis.AtlasAnalyzer;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.lucene.benchmark.byTask.feeds.DocData;
import org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException;
import org.apache.lucene.benchmark.byTask.feeds.TrecContentSource;
import org.apache.lucene.benchmark.byTask.feeds.TrecParserByPath;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Trec7Test {


    private final SentenceDetectorME sentenceSplitter;
    private TrecContentSource tcs = new TrecContentSource();
    Properties props = new Properties();
    File dataDir = new File("/home/mike/Documents/corpus/Trec7");

    public Trec7Test() throws IOException {

        String sentenceDetectModel = AtlasConfiguration.getInstance().getSentenceModel();
        String modelFolder = AtlasConfiguration.getInstance().getModelFolder();
        this.sentenceSplitter = new SentenceDetectorME(new SentenceModel(new File(modelFolder, sentenceDetectModel)));

        props.setProperty("print.props", "false");
        props.setProperty("content.source.verbose", "false");
        props.setProperty("content.source.excludeIteration", "true");
        props.setProperty("doc.maker.forever", "false");
        props.setProperty("docs.dir", dataDir.getCanonicalPath().replace('\\','/'));
        props.setProperty("trec.doc.parser", TrecParserByPath.class.getName());
        props.setProperty("content.source.forever", "false");

        tcs.setConfig(new Config(props));
        tcs.resetInputs();
    }

    public DocData getNextDoc(DocData docData) throws IOException, NoMoreDataException {
        return tcs.getNextDocData(docData);
    }

    public int getItemCount(){
        return tcs.getItemsCount();
    }

//    public Document[] getDocuments(DocData docData){
//        System.out.println(docData.getName());
//        String[] bodySentences = this.sentenceSplitter.sentDetect(docData.getBody());
//        int sentCount = bodySentences.length;
//        Document[] sentDoc = new Document[sentCount];
//        for(int i = 0; i < sentCount; i ++){
//            sentDoc[i].add(new TextField("text", bodySentences[i], Field.Store.NO));
//            sentDoc[i].add(new StoredField("docId", docData.getName()));
//        }
//
//        if(docData.getTitle() != null){
//            String[] titleSentences = this.sentenceSplitter.sentDetect(docData.getTitle());
//            int newStart = sentCount;
//            sentCount += titleSentences.length;
//            for (int i = newStart, j = 0; i < sentCount; i++, j++) {
//                sentDoc[i].add(new TextField("title", titleSentences[j],Field.Store.YES));
//                sentDoc[i].add(new StoredField("docId", docData.getName()));
//            }
//        }
//        return sentDoc;
//    }
//
//    private Document[] getDocuments(String field, String text){
//        String[] sentences = this.sentenceSplitter.sentDetect(text);
//        Document[] sentDoc = new Document[sentences.length];
//        for (int i = 0; i < sentences.length; i++) {
//            sentDoc[i].add(new TextField(field, sentences[i], Field.Store.NO));
//            sentDoc[i].add(new StoredField("docId", ))
//        }
//    }

    public List<Document> getDocuments(DocData docData) {
        String[] bodySentences = this.sentenceSplitter.sentDetect(docData.getBody());
        List<Document> documents = new ArrayList<>();
//        Document document = new Document();
        for(String body: bodySentences){
            Document document = new Document();

//            document.clear();
            document.add(new TextField("text", body, Field.Store.YES));
            document.add(new StoredField("docId", docData.getName()));
            documents.add(document);
        }
        if(docData.getTitle() != null){
            String[] titleSentences = this.sentenceSplitter.sentDetect(docData.getTitle());
            for (String title : titleSentences) {
                Document document = new Document();

//                document.clear();
                document.add(new TextField("title", title, Field.Store.YES));
                document.add(new StoredField("docId", docData.getName()));
                documents.add(document);
            }
        }
        return documents;
    }

    public Document getNextDocument(DocData docData){
        System.out.println(docData.getName());
        Document document = new Document();
        if(docData.getTitle() != null)
            document.add(new TextField("title", docData.getTitle(), Field.Store.YES));
        document.add(new TextField("text", docData.getBody(), Field.Store.NO));
        document.add(new StoredField("docId", docData.getName()));
//        document.clear();
        return document;
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Start indexing!");
        long startTime = System.currentTimeMillis();
        final Path indexDir = new File("/home/mike/Documents/Index/Trec7").toPath();
        Directory dir = NIOFSDirectory.open(indexDir);
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new AtlasAnalyzer()));

        Trec7Test trec7Test = new Trec7Test();
        DocData docData = new DocData();
        int count = 0;
        while (count < 10) {
            try {
//                System.out.println(trec7Test.getNextDoc(docData).getName());
//                writer.addDocument(trec7Test.getNextDocument(trec7Test.getNextDoc(docData)));
                writer.addDocuments(trec7Test.getDocuments(trec7Test.getNextDoc(docData)));
                System.out.println(docData.getName());
                count++;
                if (count % 100 == 0) {
                    System.out.println(count + " documents indexed.");
                }
            } catch (NoMoreDataException e) {
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Indexing " + writer.numDocs() + " documents took" + (endTime - startTime) / 1000 + " second");
        writer.close();
    }
}
