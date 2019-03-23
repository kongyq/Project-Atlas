package edu.udel.irl.atlas.search;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.Set;

public class AtlasWeight extends Weight {
    private final Query query;
    /**
     * Sole constructor, typically invoked by sub-classes.
     *
     * @param query the parent query
     */
    protected AtlasWeight(Query query) {
        super(query);
        this.query = query;
    }

    @Override
    public void extractTerms(Set<Term> terms) {

    }

    @Override
    public Explanation explain(LeafReaderContext context, int doc) throws IOException {
        return null;
    }

    @Override
    public Scorer scorer(LeafReaderContext context) throws IOException {
        return null;
    }

    @Override
    public boolean isCacheable(LeafReaderContext ctx) {
        return false;
    }
}
