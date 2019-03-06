package edu.udel.irl.atlas.babelnet;

import it.uniroma1.lcl.jlt.util.Language;

public interface SynsetOp {

    String[] getSynsetIds(String[] lemmas, String[] poses, Language language);

    String getSynsetId(String lemma, String pos, Language language);

    String[] getSynsetIds(String[] lemmas, String[] poses);

    String getSynsetId(String lemma, String pos);
}
