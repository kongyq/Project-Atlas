package edu.udel.irl.atlas.analysis;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.regex.Pattern;

public class PunctuationFilter extends FilteringTokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    /**
     * Create a new {@link FilteringTokenFilter}.
     *
     * @param in the {@link TokenStream} to consume
     */
    public PunctuationFilter(TokenStream in) {
        super(in);
    }

    @Override
    protected boolean accept(){
        return !Pattern.matches("\\p{Punct}", termAtt.subSequence(0, termAtt.length()));
    }
}
