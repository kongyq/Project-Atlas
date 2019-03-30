package edu.udel.irl.atlas.search.function;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

import java.util.List;

public class AtlasScoreFunction extends ScoreFunction{
    @Override
    public float docScore(int docId, String field, List<Term> termList, List<BytesRef> payloadList) {
        return 0;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
