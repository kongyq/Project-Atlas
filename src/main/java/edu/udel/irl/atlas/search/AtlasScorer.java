package edu.udel.irl.atlas.search;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;

public class AtlasScorer extends Scorer {
    /**
     * Constructs a Scorer
     *
     * @param weight The scorers <code>Weight</code>.
     */
    protected AtlasScorer(Weight weight) {
        super(weight);
    }

    @Override
    public int docID() {
        return 0;
    }

    @Override
    public float score() throws IOException {
        return 0;
    }

    @Override
    public DocIdSetIterator iterator() {
        return null;
    }
}
