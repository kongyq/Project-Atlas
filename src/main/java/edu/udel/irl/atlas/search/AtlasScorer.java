package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.search.function.ScoreFunction;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.SpanScorer;
import org.apache.lucene.search.spans.SpanWeight;

import java.io.IOException;
import java.util.Map;

public class AtlasScorer extends SpanScorer {

    private final AtlasWeight.PayloadSpans spans;
    private final ScoreFunction function;
    private final Map queryMap;
    private final String field;
    /**
     * Constructs a Scorer
     *
     * @param weight The scorers <code>Weight</code>.
     */
    protected AtlasScorer(SpanWeight weight, AtlasWeight.PayloadSpans spans, Similarity.SimScorer simScorer, ScoreFunction function, Map queryMap, String field) {
        super(weight, spans, simScorer);
        this.spans = spans;
        this.function = function;
        this.queryMap = queryMap;
        this.field = field;
    }

    protected float getPayloadScore() {
        return function.docScore(docID(), field, spans.termList, spans.payloadList);
    }

    protected Explanation getPayloadExplanation() {
        return function.explain(docID(), field, spans.termList, spans.payloadList);
    }

    protected float getSpanScore() throws IOException {
        return super.scoreCurrentDoc();
    }

    @Override
    protected float scoreCurrentDoc() throws IOException {
        return getPayloadScore();
    }
}
