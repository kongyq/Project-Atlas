package edu.udel.irl.atlas.analysis;

import edu.udel.irl.atlas.parser.ParserOpFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.io.IOException;
import java.util.Map;

public class ParsePayloadFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
    public static final String PARSER_MODEL = "parserModel";
    public static final String PARSER_NAME = "name";

    private final String parserName;
    private final String parserModelFile;
    /**
     * Initialize this factory via a set of key-value pairs.
     *
     * @param args
     */
    protected ParsePayloadFilterFactory(Map<String, String> args) {
        super(args);
        parserModelFile = require(args, PARSER_MODEL);
        parserName = get(args, PARSER_NAME, ParserOpFactory.OPENNLP);
        if(!args.isEmpty()){
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public void inform(ResourceLoader loader) {
        try { // load and register the read-only model in cache with file/resource name
            switch (parserName.toLowerCase()){
                case ParserOpFactory.BERKELEY:
                    ParserOpFactory.getBerkeleyModel(parserModelFile, loader);
                    break;
                case ParserOpFactory.OPENNLP:
                    ParserOpFactory.getNLPParserModel(parserModelFile, loader);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong parser name!");
            }
        }catch (IOException e){
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new ParsePayloadFilter(input, ParserOpFactory.getParser(parserName, parserModelFile));
    }
}
