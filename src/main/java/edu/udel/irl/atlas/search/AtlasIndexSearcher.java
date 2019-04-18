package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.util.AtlasConfiguration;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Deprecated
public class AtlasIndexSearcher extends IndexSearcher{

    private int numDocHits;
    private final int totalSents;
    private final String docIdField = AtlasConfiguration.getInstance().getDocIdField();
    /**
     * {@code Map<String, List<ScoreDoc>>} A docId, scored sentences map
     */
    private Object2ObjectMap<String, ObjectList<ScoreDoc>> doc2ScoreMap;

    /**
     * Creates a searcher searching the provided index
     * @param r an IndexReader
     */
    public AtlasIndexSearcher(IndexReader r) {
        this(r, null);
    }

    /** Runs searches for each segment separately, using the
     *  provided ExecutorService.  IndexSearcher will not
     *  close/awaitTermination this ExecutorService on
     *  close; you must do so, eventually, on your own.  NOTE:
     *  if you are using {@link NIOFSDirectory}, do not use
     *  the shutdownNow method of ExecutorService as this uses
     *  Thread.interrupt under-the-hood which can silently
     *  close file descriptors (see <a
     *  href="https://issues.apache.org/jira/browse/LUCENE-2239">LUCENE-2239</a>).
     */
    public AtlasIndexSearcher(IndexReader r, ExecutorService executor) {
        this(r.getContext(), executor);
    }


    /**
     * Creates a searcher searching the provided top-level {@link IndexReaderContext}.
     * <P></P>
     * Given a non-<code>null</code> {@link ExecutorService} this method runs
     *    * searches for each segment separately, using the provided ExecutorService.
     *    * IndexSearcher will not close/awaitTermination this ExecutorService on
     *    * close; you must do so, eventually, on your own. NOTE: if you are using
     *    * {@link NIOFSDirectory}, do not use the shutdownNow method of
     *    * ExecutorService as this uses Thread.interrupt under-the-hood which can
     *    * silently close file descriptors (see <a
     *    * href="https://issues.apache.org/jira/browse/LUCENE-2239">LUCENE-2239</a>).
     *    *
     *    * @see IndexReaderContext
     *    * @see IndexReader#getContext()
     */
    public AtlasIndexSearcher(IndexReaderContext context, ExecutorService executor) {
        super(context, executor);
        this.totalSents = context.reader().numDocs();
    }

    /**
     * Creates a searcher searching the provided top-level {@link IndexReaderContext}.
     *
     * @see IndexReaderContext
     * @see IndexReader#getContext()
     */
    public AtlasIndexSearcher(IndexReaderContext context) {
        this(context, null);
    }

    /**
     * Find the top <code>n</code>
     * hits for an <code>AtlasQuery</code>
     * @param query an AtlasQuery
     * @param n top n hit
     * @return an AtlasTopDocs
     * @throws IOException if the index cannot be read
     */
    public AtlasTopDocs search(SpanQuery query, int n) throws IOException {
        return search(query, n, this.docIdField);
    }

    /** Finds the top <code>n</code>
     * hits for a <code>AtlasQuery</code>.
     *
     * @throws BooleanQuery.TooManyClauses If a query would exceed
     *         {@link BooleanQuery#getMaxClauseCount()} clauses.
     */
    public AtlasTopDocs search(SpanQuery query, int n, String docIdField) throws IOException {
        // retrieve all sentences in the index.
        TopDocs allSents = super.search(query, totalSents);
        doc2ScoreMap = new Object2ObjectOpenHashMap<>();

        // create a table, and for each document, add its corresponding sentences.
        for(ScoreDoc scoreDoc: allSents.scoreDocs){
            if(scoreDoc.score == 0f) continue;

            String docId = super.doc(scoreDoc.doc).get(docIdField);
            doc2ScoreMap.putIfAbsent(docId, new ObjectArrayList<>());
            doc2ScoreMap.get(docId).add(scoreDoc);
        }

        ObjectIterator<Object2ObjectMap.Entry<String, ObjectList<ScoreDoc>>> iterator = Object2ObjectMaps.fastIterator(doc2ScoreMap);

        ObjectList<AtlasScoreDoc> atlasScoreDocsList = new ObjectArrayList<>(doc2ScoreMap.size());

        // for each documents create an AtlasScoreDoc.
        while(iterator.hasNext()){
            Object2ObjectMap.Entry<String, ObjectList<ScoreDoc>> entry = iterator.next();
            atlasScoreDocsList.add(new AtlasScoreDoc(entry.getKey(), entry.getValue().toArray(new ScoreDoc[0])));
        }

        // create an AtlasTopDocs based on total hits and all AtlasScoreDocs created above.
        return AtlasTopDocs.create(n, atlasScoreDocsList.toArray(new AtlasScoreDoc[0]));
    }

    /** Returns an Explanation that describes how <code>doc</code> scored against
     * <code>query</code>.
     *
     * <p>This is intended to be used in developing Similarity implementations,
     * and, for good performance, should not be displayed with every hit.
     * Computing an explanation is as expensive as executing the query over the
     * entire index.
     */
    public Explanation explain(SpanQuery query, AtlasScoreDoc scoreDoc) throws IOException {
        List<Explanation> sentenceExplains = new ArrayList<>(scoreDoc.scoreDocs.length);
        for(ScoreDoc sentence: scoreDoc.scoreDocs){
            sentenceExplains.add(super.explain(query, sentence.doc));
        }
        return Explanation.match(scoreDoc.score, "Final Score", sentenceExplains);
    }

    /**
     * get the number of sentences in the index
     * @return the number of sentence in the index
     */
    public int getTotalSents(){return this.totalSents;}

    /**
     * get the number of documents that hit the query
     * @return the number of documents
     */
    public int getNumDocHits(){return this.numDocHits;}
}
