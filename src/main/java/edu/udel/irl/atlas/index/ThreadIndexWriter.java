package edu.udel.irl.atlas.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadIndexWriter extends IndexWriter {

    private ExecutorService threadPool;
//    private Analyzer analyzer;

    private class Job implements Runnable{

        Document doc;
        Term delTerm;

        public Job(Document doc, Term delTerm){
            this.doc = doc;
            this.delTerm = delTerm;
        }

        @Override
        public void run() {
            try {
                if(delTerm != null) {
                    ThreadIndexWriter.super.updateDocument(delTerm, doc);
                }else{
                    ThreadIndexWriter.super.addDocument(doc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Constructs a new IndexWriter per the settings given in <code>conf</code>.
     * If you want to make "live" changes to this writer instance, use
     * {@link #getConfig()}.
     *
     * <p>
     * <b>NOTE:</b> after ths writer is created, the given configuration instance
     * cannot be passed to another writer.
     *
     * @param dir  the index directory. The index is either created or appended
     *             according <code>conf.getOpenMode()</code>.
     * @param conf the configuration settings according to which IndexWriter should
     *             be initialized.
     * @throws IOException if the directory cannot be read/written to, or if it does not
     *                     exist and <code>conf.getOpenMode()</code> is
     *                     <code>OpenMode.APPEND</code> or if there is any other low-level
     *                     IO error
     */
    public ThreadIndexWriter(Directory dir, IndexWriterConfig conf, int numThreads, int maxQueueSize) throws IOException {
        super(dir, conf);
        this.threadPool = new ThreadPoolExecutor(
                numThreads, numThreads,
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(maxQueueSize, false),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void addDocument(Document document){
        this.threadPool.execute(new Job(document, null));
    }

    public void updateDocument(Term term, Document document){
        this.threadPool.execute(new Job(document, term));
    }

    private void finish(){
        threadPool.shutdown();
        while (true){
            try {
                if (threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        finish();
        super.close();
    }

    public void rollback() throws IOException {
        finish();
        super.rollback();
    }
}
