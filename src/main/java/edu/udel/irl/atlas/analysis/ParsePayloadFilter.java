package edu.udel.irl.atlas.analysis;

import edu.udel.irl.atlas.parser.ParserOp;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.opennlp.OpenNLPTokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
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
 *          XX0000
 *          XX0010
 *          XX0020
 *          XX0030
 *          XX0100
 *          XX01100
 *          XX011100
 *          XX011110
 *          XX011120
 *          XX020
 *          XX: byte code of sentence index (short type)
 *    In order to quickly compute the short distance between any two tokens.
 *    <P>
 *        Tags all terms in the TypeAttribute.
 *        Encode all terms in the PayloadAttribute.
 *    </P>
 * </P>
 */
public final class ParsePayloadFilter extends TokenFilter {

    private final ParserOp.OpParser opParser;

    private final PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final FlagsAttribute flagsAtt = addAttribute(FlagsAttribute.class);

//    private List<AttributeSource> sentenceTokenAttrs = new ArrayList<>();
    private int tokenNum = 0;
    private short sentNum = 0;

    private boolean moreTokensAvailable = true;

    private String[] sentence = new String[0];
    private String[] poses = null;
    private List<byte[]> codes = new ArrayList<>();

    public ParsePayloadFilter(TokenStream input, ParserOp parserOp){
        super(input);
        this.opParser = parserOp.createParser();
    }

    private String[] nextSentence() throws IOException {
        List<String> termList = new ArrayList<>();
//        sentenceTokenAttrs.clear();
        boolean endOfSentence = false;
        while(!endOfSentence && (moreTokensAvailable = input.incrementToken())){
            termList.add(termAtt.toString());
            endOfSentence = 0 != (flagsAtt.getFlags() & OpenNLPTokenizer.EOS_FLAG_BIT);
//            sentenceTokenAttrs.add(input.cloneAttributes());
        }
        return termList.size() > 0 ? termList.toArray(new String[0]) : null;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if(!moreTokensAvailable){
            clear();
            return false;
        }

        if(tokenNum == sentence.length){    // beginning of stream, or previous sentence exhausted
            String[] origSentence = nextSentence();
            if(origSentence == null){
                clear();
                return false;
            }

            // Refactored: add sentence index to the head of each code.
            this.opParser.parseSent(origSentence);
            sentence = this.opParser.getSentence(); //some parser may reform the sentence
            poses = this.opParser.getPosTags();
            codes = this.opParser.getCodeList(sentNum);

            tokenNum = 0;
            sentNum++;
        }

        clearAttributes();
        //Since some parser (OpenNLP) may reform the sentence, this may change the length of the sentence.
        //Thus the offset attribute will be compromised, I have to discard this attribute.
        //And reset all attributes.
//        sentenceTokenAttrs.get(tokenNum).copyTo(this);

        //Set all attributes
        if(tokenNum == sentence.length-1)
            flagsAtt.setFlags(flagsAtt.getFlags() | OpenNLPTokenizer.EOS_FLAG_BIT);
        termAtt.setEmpty().append(sentence[tokenNum]);  //reset the token term
        typeAtt.setType(poses[tokenNum]);     //tags the token
        payloadAtt.setPayload(new BytesRef(codes.get(tokenNum)));       //encode the token
        tokenNum++;
        return true;
    }

    private void clear(){
//        sentenceTokenAttrs.clear();
        sentence = new String[0];
        poses = null;
        codes.clear();
        tokenNum = 0;
        sentNum = 0;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        moreTokensAvailable = true;
        clear();
    }
}
