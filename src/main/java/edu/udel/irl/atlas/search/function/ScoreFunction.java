package edu.udel.irl.atlas.search.function;

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
     * @param termList The list of matching terms
     * @param payloadList The list of payload of matching terms
     * @return The new current score
     */
    public abstract float docScore(int docId, String field, List<Term> termList, List<BytesRef> payloadList, Map<Term, ? extends Map<BytesRef, Float>> queryMap);

    public Explanation explain(int docId, String field, List<Term> termList, List<BytesRef> payloadList, Map<Term, ? extends Map<BytesRef, Float>> queryMap) {
        return Explanation.match(docScore(docId, field, termList, payloadList, queryMap),
                getClass().getSimpleName() + ".docScore()");
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);
}
