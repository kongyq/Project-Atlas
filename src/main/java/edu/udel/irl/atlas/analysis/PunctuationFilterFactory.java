package edu.udel.irl.atlas.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.io.IOException;
import java.util.Map;

public class PunctuationFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

    /**
     * Initialize this factory via a set of key-value pairs.
     *
     * @param args
     */
    protected PunctuationFilterFactory(Map<String, String> args) {
        super(args);
    }

    protected PunctuationFilterFactory(){this(null);}

    @Override
    public void inform(ResourceLoader loader) throws IOException {

    }

    @Override
    public TokenStream create(TokenStream input) {
        return new PunctuationFilter(input);
    }
}
