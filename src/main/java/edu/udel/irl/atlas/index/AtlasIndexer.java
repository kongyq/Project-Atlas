package edu.udel.irl.atlas.index;

import edu.udel.irl.atlas.analysis.AtlasAnalyzer;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.*;
import java.nio.file.Files;

public class AtlasIndexer {

    private String docIdField = AtlasConfiguration.getInstance().getIndexDocIdField();
    private String textField = AtlasConfiguration.getInstance().getIndexBodyField();
    private final IndexWriter writer;
    private final SentenceDetectorME sentenceSplitter;

    public AtlasIndexer(String indexDir) throws IOException {
        Directory dir = NIOFSDirectory.open(new File(indexDir).toPath());
        this.writer = new IndexWriter(dir, new IndexWriterConfig(new AtlasAnalyzer()));

        String sentenceDetectModel = AtlasConfiguration.getInstance().getSentenceModel();
        String modelFolder = AtlasConfiguration.getInstance().getModelFolder();
        this.sentenceSplitter = new SentenceDetectorME(new SentenceModel(new File(modelFolder, sentenceDetectModel)));
    }

    public AtlasIndexer(String indexDir, String docIdField, String textField) throws IOException {
        this(indexDir);
        this.docIdField = docIdField;
        this.textField = textField;
    }

    private static class TextFilesFilter implements FileFilter{
        @Override
        public boolean accept(File file) {
            return file.getName().toLowerCase().endsWith(".txt");
        }
    }

    private Document[] getSentence(File file) throws IOException {
        String docText = new String(Files.readAllBytes(file.toPath()));
        String[] sentences = this.sentenceSplitter.sentDetect(docText);
        Document[] sentDoc = new Document[sentences.length];
        for (int i = 0; i < sentences.length; i++) {
            sentDoc[i].add(new TextField(textField, sentences[i], Field.Store.NO));
            sentDoc[i].add(new StoredField(docIdField, file.getName()));
        }
        return sentDoc;
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing " + file.getCanonicalPath());
        for(Document sent: getSentence(file))
            writer.addDocument(sent);
    }

    public int index(String dataDir) throws IOException {return index(dataDir, null);}

    /**
     * Index all files in the data folder with a specified file filter
     * @param dataDir the corpus folder path
     * @param filter a file filter
     * @return the number of sentences indexed
     * @throws IOException if the index writer cannot index the file
     */
    public int index(String dataDir, FileFilter filter) throws IOException {
        File[] files = new File(dataDir).listFiles(filter);
        for(File file: files){
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()){
                indexFile(file);
            }
        }
        return writer.numDocs();
    }

    /**
     * Closes all open resources and releases the write lock.
     * this will attempt to gracefully shut down by writing any changes,
     * waiting for any running merges, committing, and closing.
     * @throws IOException
     */
    public void close() throws IOException {this.writer.close();}

}
