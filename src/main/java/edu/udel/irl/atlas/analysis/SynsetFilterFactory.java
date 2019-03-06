package edu.udel.irl.atlas.analysis;

import edu.udel.irl.atlas.babelnet.SynsetOpFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.io.IOException;
import java.util.Map;

public class SynsetFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

    public static final String DICT_NAME = "name";
    public static final String POSMAPPING_FILENAME = "mappingFile";

    private final String synsetDictName;
    private final String posMappingFileName;

    /**
     * Initialize this factory via a set of key-value pairs.
     *
     * @param args
     */
    public SynsetFilterFactory(Map<String, String> args){
        super(args);
        synsetDictName = get(args, DICT_NAME, SynsetOpFactory.WORDNET);
        posMappingFileName = get(args, POSMAPPING_FILENAME, "en-wn.map");
        if(!args.isEmpty()){
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public void inform(ResourceLoader loader){
        // load and register the read-only model in cache with file/resource name
        try {
            SynsetOpFactory.getUposMapper(posMappingFileName, loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        try {
            return new SynsetFilter(input, SynsetOpFactory.getSynset(synsetDictName, posMappingFileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
