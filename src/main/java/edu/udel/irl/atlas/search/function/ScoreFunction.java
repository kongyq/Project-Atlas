package edu.udel.irl.atlas.search.function;

import edu.udel.irl.atlas.search.AtlasBar;
import edu.udel.irl.atlas.search.AtlasScoreData;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.util.BytesRef;

import java.util.List;
import java.util.Map;

/**
 * An abstract class that defines a way for AtlasQuery instances to calculate
 * the score by using payloads and terms for a document.
 *
 * @see edu.udel.irl.atlas.search.AtlasQuery for more information
 *
 **/
public abstract class ScoreFunction {

    /**
     * Calculate the final score with all the payloads and terms seen so far for this doc/field
     * @param docId The current doc
     * @param field The field
     * @param bars The list of AtlasBars that contains all matching data (payloads for both doc and query, and weight)
     * @return The new current score
     */
    public abstract float docScore(
            int docId,
            String field,
            List<AtlasBar> bars);

    public Explanation explain(
            int docId,
            String field,
            List<AtlasBar> bars) {
        return Explanation.match(docScore(docId, field, bars),
                getClass().getSimpleName() + ".docScore()");
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);
}
