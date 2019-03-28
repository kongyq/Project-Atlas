package edu.udel.irl.atlas.analysis;

import edu.udel.irl.atlas.babelnet.SynsetOp;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

/***
 * <P>Run WSD module. convert all terms in their corresponding synset.</P>
 * <P>
 *     This filter need pos and lemma to retrieve the synsetId.
 *     A POS or parser filter and a lemma filter need to be performed before this filter.
 * </P>
 */
public final class SynsetFilter extends TokenFilter {

    private final SynsetOp synsetOp;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    public SynsetFilter(TokenStream input, SynsetOp synsetOp){
        super(input);
        this.synsetOp = synsetOp;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if(!input.incrementToken()){
            return false;
        }
        String synsetId = synsetOp.getSynsetId(termAtt.toString(), typeAtt.type());
        termAtt.setEmpty().append(synsetId);
        return true;
    }
}
