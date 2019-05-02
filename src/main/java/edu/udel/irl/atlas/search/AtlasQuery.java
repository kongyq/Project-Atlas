package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.search.function.ScoreFunction;
import edu.udel.irl.atlas.util.Short2Bytes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

/**
 * A Query class that uses a {@link ScoreFunction} to compute the socre of a wrapped SpanQuery.
 * Atlas query that wraps another query, and uses AtlasWeight and AtlasScorer to calculate the final score.
 *
 * This will call wrapped query matches method and uses the return matchesIterator.
 */
//REFACTORED!!
public class AtlasQuery extends SpanQuery {

    private final SpanQuery wrappedSpanQuery;
    private final Map<Term, ? extends Map<BytesRef, Float>> queryMap;
    private final ScoreFunction function;
    private final boolean includeSpanScore;
    private final boolean includePayloadScore;

    /**
     * Create a new AtlasQuery.
     * You should use {@link #AtlasQuery(SpanQuery, ScoreFunction, Map)} instead.
     * Since the original score of the SpanQuery is no need for final score computation in Atlas.
     *
     * @param in the query to wrap
     * @param function a ScoreFunction to use to compute the final scores
     * @param inMap a HashMap to use to obtain the similarity of the matching terms and their query payloads
     * @param includeSpanScore include both span score and Atlas score in the final score ({@code false} as default)
     */
    public AtlasQuery(SpanQuery in, ScoreFunction function, Map<Term, ? extends Map<BytesRef, Float>>  inMap, boolean includeSpanScore){
        this(in, function, inMap, includeSpanScore, true);
    }

    public AtlasQuery(SpanQuery in, ScoreFunction function, Map<Term, ? extends Map<BytesRef, Float>> inMap, boolean includeSpanScore, boolean includePayloadScore) {
        assert includePayloadScore || includeSpanScore : "Error: at least include either span score or payload score!";
        this.wrappedSpanQuery = in;
        this.function = function;
        this.queryMap = inMap;
        //Span score does not used as default and should be that way.
        this.includeSpanScore = includeSpanScore;
        this.includePayloadScore = includePayloadScore;
    }

    /**
     * Create a new AtlasQuery.
     * @param in the query to wrap
     * @param function a ScoreFunction to use to compute the final scores
     * @param inMap a HashMap to use to obtain the similarity of the matching terms and their query payloads
     */
    public AtlasQuery(SpanQuery in, ScoreFunction function, Map<Term, ? extends Map<BytesRef, Float>>  inMap){
        this(in, function, inMap, false);
    }

    /**
     * Create a new AtlasQuery.
     * You should use {@link #AtlasQuery(SpanQuery, ScoreFunction, Map)} instead.
     * This constructor only used for test only.
     * @param in the query to wrap
     * @param function a ScoreFunction to use to compute the final scores
     */
    public AtlasQuery(SpanQuery in, ScoreFunction function){
        this(in, function, new Object2ObjectOpenHashMap<>());

    }

    @Override
    public String toString(String field){
        return  "AtlasQuery(" +
                wrappedSpanQuery.toString(field) +
                ", function: " +
                function.getClass().getSimpleName() +
                ")";
    }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass() && equalsTo(getClass().cast(other));
    }

    private boolean equalsTo(AtlasQuery other) {
        return wrappedSpanQuery.equals(other.wrappedSpanQuery) &&
                function.equals(other.function) && (queryMap == other.queryMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrappedSpanQuery, function, queryMap);
    }

    @Override
    public String getField() {
        return wrappedSpanQuery.getField();
    }

    @Override
    public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        SpanWeight innerWeight = wrappedSpanQuery.createWeight(searcher, needsScores, boost);
        if (!needsScores)
            return innerWeight;
        return new AtlasWeight(searcher, innerWeight, boost);
    }

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        Query matchRewritten = wrappedSpanQuery.rewrite(reader);
        if (wrappedSpanQuery != matchRewritten && matchRewritten instanceof SpanQuery) {
            return new AtlasQuery((SpanQuery)matchRewritten, function, queryMap);
        }
        return super.rewrite(reader);
    }

    //Combine AtlasWeight as inner private class

    private class AtlasWeight extends SpanWeight{

        private final SpanWeight innerWeight;

        public AtlasWeight(IndexSearcher searcher, SpanWeight innerWeight, float boost) throws IOException {
            super(AtlasQuery.this, searcher, null, boost);
            this.innerWeight = innerWeight;
        }

        @Override
        public void extractTermContexts(Map<Term, TermContext> contexts) {
            innerWeight.extractTermContexts(contexts);
        }

        @Override
        public Spans getSpans(LeafReaderContext ctx, Postings requiredPostings) throws IOException {
            return innerWeight.getSpans(ctx, requiredPostings.atLeast(Postings.PAYLOADS));
        }

        @Override
        public void extractTerms(Set<Term> terms) {
            innerWeight.extractTerms(terms);
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return innerWeight.isCacheable(ctx);
        }

        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            AtlasScorer scorer = (AtlasScorer) scorer(context);
            if (scorer == null || scorer.iterator().advance(doc) != doc)
                return Explanation.noMatch("No match");

            scorer.score();  // force freq calculation
            Explanation payloadExpl = scorer.getPayloadExplanation();

            //this may be deprecated in the future. if span score is needed no more.
            if (includeSpanScore) {
                SpanWeight innerWeight = ((AtlasWeight) scorer.getWeight()).innerWeight;
                Explanation innerExpl = innerWeight.explain(context, doc);
                return Explanation.match(scorer.scoreCurrentDoc(), "AtlasQuery, product of:", innerExpl, payloadExpl);
            }

            return payloadExpl;
        }

        @Override
        public SpanScorer scorer(LeafReaderContext context) throws IOException {
            Spans spans = getSpans(context, Postings.PAYLOADS);
            if (spans == null)
                return null;
            // similarity did not used in AtlasQuery, may deprecated in the future.
            Similarity.SimScorer docScorer = innerWeight.getSimScorer(context);
            PayloadSpans payloadSpans = new PayloadSpans(spans);
            return new AtlasScorer(this, payloadSpans, docScorer);
        }
    }

    //Combine PayloadSpans as inner private class
    private class PayloadSpans extends FilterSpans implements SpanCollector {

        public List<AtlasBar> bars = new ArrayList<>();

        private PayloadSpans(Spans in) {
            super(in);
        }

        @Override
        protected AcceptStatus accept(Spans candidate){
            return AcceptStatus.YES;
        }

        @Override
        protected void doStartCurrentDoc() {
            bars.clear();
        }

        @Override
        public void collectLeaf(PostingsEnum postings, int position, Term term) throws IOException {
            if(queryMap.containsKey(term)){
                for(Map.Entry<BytesRef, Float> entry : queryMap.get(term).entrySet()){
                    bars.add(new AtlasBar(entry.getValue(), postings.getPayload(), entry.getKey()));
                }
            }
        }

        @Override
        public void reset() {}

        @Override
        protected void doCurrentSpans() throws IOException {
            in.collect(this);
        }
    }

    //Combine AtlasScorer as inner private class
    private class AtlasScorer extends SpanScorer{

        private final PayloadSpans spans;

        public AtlasScorer(SpanWeight weight, PayloadSpans spans, Similarity.SimScorer docScorer){
            super(weight, spans, docScorer);
            this.spans = spans;
        }

        protected float getPayloadScore() {
            return function.docScore(docID(), getField(), spans.bars);
        }

        protected Explanation getPayloadExplanation() {
            return function.explain(docID(), getField(), spans.bars);
        }

        //This may be deprecated in the future if span score is needed no more.
        protected float getSpanScore() throws IOException {
            return super.scoreCurrentDoc();
        }

        @Override
        protected float scoreCurrentDoc() throws IOException {
            //This may be deprecated in the future if span score is needed no more.
            if(includeSpanScore && includePayloadScore) return (getSpanScore()) * (1 + getPayloadScore());
            if(includeSpanScore) return getSpanScore();
            return getPayloadScore();
//            if (includeSpanScore)
//                return getSpanScore() * (1 + getPayloadScore());
//            return getPayloadScore();
        }
    }
}
