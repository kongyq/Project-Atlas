package edu.udel.irl.atlas.search.function;

import edu.udel.irl.atlas.search.AtlasBar;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

import java.util.List;
import java.util.Map;

public class PlexScoreFunction extends ScoreFunction {


    @Override
    public float docScore(int docId, String field, List<AtlasBar> bars) {
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
