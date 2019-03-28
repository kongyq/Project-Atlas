package edu.udel.irl.atlas.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/***
 *  A Java implementation for slavpetrov's universal pos tags converter.
 *  Interface for converting POS tags from various treebanks
 * to the universal tagset of Petrov, Das, & McDonald.
 * The tagset consists of the following 12 coarse tags:
 * VERB - verbs (all tenses and modes)
 * NOUN - nouns (common and proper)
 * PRON - pronouns
 * ADJ - adjectives
 * ADV - adverbs
 * ADP - adpositions (prepositions and postpositions)
 * CONJ - conjunctions
 * DET - determiners
 * NUM - cardinal numbers
 * PRT - particles or other function words
 * X - other: foreign words, typos, abbreviations
 * . - punctuation
 */

public class UPOSMapper {
    private static ConcurrentHashMap<String, String> map;

    /**
     * <P>Initialize the class with map file</P>
     * @param mappingFile The mapping file path.
     */
    public UPOSMapper(String mappingFile){
        map = new ConcurrentHashMap<>();
        try {
            for(String line: Files.readAllLines(Paths.get(mappingFile))){
                line = line.trim();
                if(line.isEmpty()) continue;
                String[] tags = line.split("\t");
                map.put(tags[0], tags[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UPOSMapper(File mappingFile){
        this(mappingFile.getAbsolutePath());
    }

    public UPOSMapper(InputStream input){
        map = new ConcurrentHashMap<>();
        for(String line: new BufferedReader(new InputStreamReader(input)).lines().toArray(String[]::new)){
            line = line.trim();
            if(line.isEmpty()) continue;
            String[] tags = line.split("\t");
            map.put(tags[0], tags[1]);
        }
    }

    /**
     * <P>Produces the (coarse) universal tag given an original POS tag from the treebank in question.</P>
     * <P>
     *     >>> convert("VBZ")
     *     u"VERB"
     *     >>> convert("VBP")
     *     u"VERB"
     *     >>> convert("``")
     *     u"."
     * </P>
     * @param originalTag The treebank tag needs to converted.
     * @return The corresponding universal pos tag for the original tag.
     */
    public String convert(String originalTag){
        return map.getOrDefault(originalTag, null);
    }
}
