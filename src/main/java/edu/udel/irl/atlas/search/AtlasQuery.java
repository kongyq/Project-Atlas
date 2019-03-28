package edu.udel.irl.atlas.search;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
public class AtlasQuery extends SpanQuery {

//    private final Map<String, String>
    private final SpanQuery wrappedSpanQuery;
    private final Map queryMap;

    public AtlasQuery(SpanQuery in, Map inMap){
        this.wrappedSpanQuery = in;
        this.queryMap = inMap;
    }

    public AtlasQuery(SpanQuery in){
        this(in, new Int2ObjectOpenHashMap());
    }

    public SpanQuery getWrappedQuery() {return wrappedSpanQuery;}

    @Override
    public String toString(String field) {
        return "AtlasQuery(" + wrappedSpanQuery.toString(field) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        AtlasQuery that = (AtlasQuery) obj;
        return Objects.equals(wrappedSpanQuery, that.wrappedSpanQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrappedSpanQuery);
    }


    @Override
    public String getField() {
        return null;
    }

    @Override
    public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        return null;
    }
}
