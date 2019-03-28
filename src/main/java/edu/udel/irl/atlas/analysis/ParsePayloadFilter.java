package edu.udel.irl.atlas.analysis;

import edu.udel.irl.atlas.parser.ParserOp;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.opennlp.OpenNLPTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/***
 * <P>Run OpenNLP parser or Berkeley parser, then encode each token with the parsing information.</P>
 * <P>
 *    Each token's parsing information encoded into byte array.
 *    E.g.
 *          (ROOT (S (NP (DT The) (JJ quick) (JJ brown) (NN fox)) (VP (VBZ jumps) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))
 *          0000
 *          0010
 *          0020
 *          0030
 *          0100
 *          01100
 *          011100
 *          011110
 *          011120
 *          020
 *    In order to quickly compute the short distance between any two tokens.
 *    <P>
 *        Tags all terms in the TypeAttribute.
 *        Encode all terms in the PayloadAttribute.
 *    </P>
 * </P>
 */
public final class ParsePayloadFilter extends TokenFilter {

    private final ParserOp parserOp;

    private final PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final FlagsAttribute flagsAtt = addAttribute(FlagsAttribute.class);

    private List<AttributeSource> sentenceTokenAttrs = new ArrayList<>();
    private int tokenNum = 0;

    private boolean moreTokensAvailable = true;
    private String[] poses = null;
    private List<byte[]> codes = new ArrayList<>();

    public ParsePayloadFilter(TokenStream input, ParserOp parserOp){
        super(input);
        this.parserOp = parserOp;
    }

    private String[] nextSentence() throws IOException {
        List<String> termList = new ArrayList<>();
        sentenceTokenAttrs.clear();
        boolean endOfSentence = false;
        while(!endOfSentence && (moreTokensAvailable = input.incrementToken())){
            termList.add(termAtt.toString());
            endOfSentence = 0 != (flagsAtt.getFlags() & OpenNLPTokenizer.EOS_FLAG_BIT);
            sentenceTokenAttrs.add(input.cloneAttributes());
        }
        return termList.size() > 0 ? termList.toArray(new String[0]) : null;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if(!moreTokensAvailable){
            clear();
            return false;
        }

        if(tokenNum == sentenceTokenAttrs.size()){  // beginning of stream, or previous sentence exhausted
            String[] sentence = nextSentence();
            if(sentence == null){
                clear();
                return false;
            }
            this.parserOp.parseSent(sentence);
            poses = this.parserOp.getPosTags();
            codes = this.parserOp.getCodeList();
            tokenNum = 0;
        }

        clearAttributes();
        sentenceTokenAttrs.get(tokenNum).copyTo(this);
        typeAtt.setType(poses[tokenNum]);     //tags the token
        payloadAtt.setPayload(new BytesRef(codes.get(tokenNum)));       //encode the token
        tokenNum++;
        return true;
    }

    private void clear(){
        sentenceTokenAttrs.clear();
        poses = null;
        codes.clear();
        tokenNum = 0;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        moreTokensAvailable = true;
        clear();
    }
}
