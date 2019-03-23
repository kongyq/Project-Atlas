package edu.udel.irl.atlas.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/***
 * Atlas query that wraps another query, and uses AtlasWeight and AltasScorer to calculate the final score.
 *
 * This will call wrapped query matches method and uses the return matchesIterator.
 */
public class AtlasQuery extends Query {
//    private final Map<String, String>
    private final Query wrappedQuery;

    public AtlasQuery(Query in){
        this.wrappedQuery = in;
    }

    public Query getWrappedQuery() {return wrappedQuery;}

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        return new AtlasQuery(wrappedQuery.rewrite(reader));
    }

    @Override
    public String toString(String field) {
        return "AtlasQuery(" + wrappedQuery.toString(field) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        AtlasQuery that = (AtlasQuery) obj;
        return Objects.equals(wrappedQuery, that.wrappedQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrappedQuery);
    }


}
