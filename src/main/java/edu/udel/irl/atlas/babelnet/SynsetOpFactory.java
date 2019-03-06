package edu.udel.irl.atlas.babelnet;

import edu.udel.irl.atlas.util.UPOSMapper;
import org.apache.lucene.analysis.util.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supply WSDed synset
 * BabelNet and WordNet synset dictionaries are supported.
 */
public class SynsetOpFactory {
    private static Map<String, UPOSMapper> uposMapperMap = new ConcurrentHashMap<>();

    public static final String BABELNET = "babelnet";
    public static final String WORDNET = "wordnet";
    public static final String MIXED = "mixed";

    public static SynsetOp getSynset(String dictName, String posMapper) throws IOException {
        switch (dictName){
            case BABELNET:
                if(posMapper != null){
                    return new BNSynsetOp(uposMapperMap.get(posMapper));
                }else{
                    return new BNSynsetOp();
                }
            case WORDNET:
                if(posMapper != null){
                    return new JWISynsetOp(uposMapperMap.get(posMapper));
                }else{
                    return new JWISynsetOp();
                }
            case MIXED:
                if(posMapper != null){
                    return new BNWNSynsetOp(uposMapperMap.get(posMapper));
                }else{
                    return new BNWNSynsetOp();
                }
            default:
                throw new IllegalArgumentException("Wrong synset dictionary name!");
        }
    }

    public static UPOSMapper getUposMapper(String mapperFileName, ResourceLoader loader) throws IOException {
        UPOSMapper mapper = uposMapperMap.get(mapperFileName);
        if(mapper == null){
            try(InputStream resource = loader.openResource(mapperFileName)){
                mapper = new UPOSMapper(mapperFileName);
            }
            uposMapperMap.put(mapperFileName, mapper);
        }
        return mapper;
    }

    // keeps unit test from blowing out momery
    public static void clearModels(){
        uposMapperMap.clear();
    }
}
