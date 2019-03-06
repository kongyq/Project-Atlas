package edu.udel.irl.atlas.parser;

import edu.berkeley.nlp.PCFGLA.ParserData;
import opennlp.tools.parser.ParserModel;
import org.apache.lucene.analysis.util.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supply Parser
 * Berlekey and OpenNLP parsers are supported.
 * Cache model file object. Assumues models files are thread-safe.
 */
public class ParserOpFactory {
    private static Map<String, ParserData> berkeleyParserModels = new ConcurrentHashMap<>();
    private static Map<String, ParserModel> NLPParserModels = new ConcurrentHashMap<>();

    public static final String BERKELEY = "berkeley";
    public static final String OPENNLP = "opennlp";

    public static ParserOp getParser(String parserName, String modelName){
        switch (parserName){
            case BERKELEY:
                if(modelName != null) {
                    ParserData model = berkeleyParserModels.get(modelName);
                    return new BerkeleyParserOp(model);
                }else{
                    return new BerkeleyParserOp();
                }
            case OPENNLP:
                if(modelName != null) {
                    ParserModel model = NLPParserModels.get(modelName);
                    return new NLPParserOp(model);
                }else{
                    return new NLPParserOp();
                }
            default:
                throw new IllegalArgumentException("Wrong parser name!");
        }
    }

    public static ParserData getBerkeleyModel(String modelName, ResourceLoader loader) throws IOException {
        ParserData model = berkeleyParserModels.get(modelName);
        if(model == null){
            String resource = ClassLoader.getSystemResource(modelName).getFile();
            if(resource == null){
                throw new IOException("Model file does not find!");
            }
            model = ParserData.Load(resource);
            berkeleyParserModels.put(modelName, model);
        }
        return model;
    }

    public static ParserModel getNLPParserModel(String modelName, ResourceLoader loader) throws IOException {
        ParserModel model = NLPParserModels.get(modelName);
        if(model == null){
            try(InputStream resource = loader.openResource(modelName)){
                model = new ParserModel(resource);
            }
            NLPParserModels.put(modelName, model);
        }
        return model;
    }

    // keeps unit test from blowing out momery
    public static void clearModels(){
        berkeleyParserModels.clear();;
        NLPParserModels.clear();
    }

}
