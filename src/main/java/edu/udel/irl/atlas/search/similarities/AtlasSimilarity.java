package edu.udel.irl.atlas.search.similarities;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

public class AtlasSimilarity extends Similarity {
    @Override
    public long computeNorm(FieldInvertState state) {
        return 0;
    }

    @Override
    public SimWeight computeWeight(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        return null;
    }

    @Override
    public SimScorer simScorer(SimWeight weight, LeafReaderContext context) throws IOException {
        return null;
    }
}
