package edu.udel.irl.atlas.search;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanWeight;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/***
 * Atlas query that wraps another query, and uses AtlasWeight and AltasScorer to calculate the final score.
 *
 * This will call wrapped query matches method and uses the return matchesIterator.
 */
public class AtlasQuery extends PayloadScoreQuery {

    private final SpanQuery wrappedSpanQuery;
    private final Map queryMap;
    private PayloadFunction function;

    public AtlasQuery(SpanQuery in, PayloadFunction function, Map inMap){
        super(in, function, null,false);
        this.wrappedSpanQuery = in;
        this.function = function;
        this.queryMap = inMap;
    }

    public AtlasQuery(SpanQuery in, PayloadFunction function){
        this(in, function, new Int2ObjectOpenHashMap());
    }

    @Override
    public String toString(String field){
        StringBuilder buffer = new StringBuilder();
        buffer.append("AtlasQuery(");
        buffer.append(wrappedSpanQuery.toString(field));
        buffer.append(", function: ");
        buffer.append(function.getClass().getSimpleName());
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) &&
                equalsTo(getClass().cast(other));
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
    public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        SpanWeight innerWeight = wrappedSpanQuery.createWeight(searcher, needsScores, boost);
        if (!needsScores)
            return innerWeight;
        return new AtlasWeight(this, searcher, innerWeight, boost);
    }
}
