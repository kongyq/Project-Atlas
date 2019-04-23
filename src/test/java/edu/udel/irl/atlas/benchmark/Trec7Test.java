package edu.udel.irl.atlas.benchmark;

import edu.udel.irl.atlas.analysis.AtlasAnalyzer;
import edu.udel.irl.atlas.index.ThreadIndexWriter;
import edu.udel.irl.atlas.util.AtlasConfiguration;
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
import java.util.Properties;

public class Trec7Test {


//    private final SentenceDetectorME sentenceSplitter;
    private TrecContentSource tcs = new TrecContentSource();
    Properties props = new Properties();
    File dataDir = new File("/home/mike/Documents/corpus/Trec7");

    public Trec7Test() throws IOException {

        String sentenceDetectModel = AtlasConfiguration.getInstance().getSentenceModel();
        String modelFolder = AtlasConfiguration.getInstance().getModelFolder();
//        this.sentenceSplitter = new SentenceDetectorME(new SentenceModel(new File(modelFolder, sentenceDetectModel)));

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


    public Document getNextDocument(DocData docData){
//        System.out.println(docData.getName());
        Document document = new Document();
        System.out.println(tcs.getConfig().get("trec.doc.parser", "!!"));
//        System.out.println(docData.getBody());
        if(!docData.getTitle().isEmpty()) {
//            System.out.println(docData.getTitle());
            document.add(new TextField("title", docData.getTitle(), Field.Store.YES));
        }
        document.add(new TextField("text", docData.getBody(), Field.Store.NO));
        document.add(new StoredField("docId", docData.getName()));
//        document.clear();
        return document;
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Start indexing!");
        long startTime = System.currentTimeMillis();
        final Path indexDir = new File("/home/mike/Documents/Index/test").toPath();
        Directory dir = NIOFSDirectory.open(indexDir);
        IndexWriterConfig iwc = new IndexWriterConfig(new AtlasAnalyzer());
        iwc.setRAMBufferSizeMB(256d);
        IndexWriter writer = new ThreadIndexWriter(dir, iwc, 4, 20);
//        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new AtlasAnalyzer()));

        Trec7Test trec7Test = new Trec7Test();
        DocData docData = new DocData();
        int count = 0;
//
//        trec7Test.getNextDoc(docData);
//        while (!docData.getName().equals("FR940706-0-00067")) {
//            trec7Test.getNextDoc(docData);
//        }

        while (true) {
            try {
//                DocData dd = trec7Test.getNextDoc(docData);
//                System.out.println(dd.getName());
//                System.out.println(dd.getBody());
//                writer.addDocument(trec7Test.getNextDocument(dd));
                writer.addDocument(trec7Test.getNextDocument(trec7Test.getNextDoc(docData)));
//                System.out.println(docData.getName());
                count++;
                if (count % 1000 == 0) {
                    System.out.println("Current indexing document : " + docData.getName());
                    System.out.println("Current indexed documents : " + writer.numDocs());
                    System.out.println(count + " documents indexed.");
                    System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime) / 60000 + " minutes");
                }
            } catch (NoMoreDataException e){
                break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Indexing " + writer.numDocs() + " documents took" + (endTime - startTime) / 1000 + " second");
        writer.close();
    }
}
