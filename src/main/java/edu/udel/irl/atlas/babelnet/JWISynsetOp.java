package edu.udel.irl.atlas.babelnet;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.udel.irl.atlas.util.UPOSMapper;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/***
 * <P>Supply JWI most common synset ID for the lemma and POS</P>
 * <P>
 *     WordNet dictionary need to be installed in the system environment or pass the folder of dict to the constructor.
 * </P>
 * <P>
 *     Since the treebank pos tag need to be convert into simple pos tag,
 *     an UPOSmapper instance need to be pass to the constructor.
 * </P>
 */
public class JWISynsetOp implements SynsetOp {
    private final UPOSMapper uposMapper;
    private final IRAMDictionary dictionary;

    public JWISynsetOp(UPOSMapper uposMapper) throws IOException {
        this.uposMapper = uposMapper;
        if(System.getenv("WNHOME") != null) {
            this.dictionary = new RAMDictionary(
                    new File(System.getenv("WNHOME") + File.separator + "dict"),
                    ILoadPolicy.IMMEDIATE_LOAD);
        }else{
            String wordnetFolder = ClassLoader.getSystemResource("wordnet/dict").getFile();
            if(wordnetFolder != null) {
                this.dictionary = new RAMDictionary(new File(wordnetFolder), ILoadPolicy.IMMEDIATE_LOAD);
            }else{
                throw new IOException("WordNet dict folder not found!");
            }
        }
        dictionary.open();
    }

    public JWISynsetOp() throws IOException {
        this.uposMapper = null;
        this.dictionary = null;
//        this(new UPOSMapper("en-wn"));

    }

    public JWISynsetOp(UPOSMapper uposMapper, File wordnetFolder) throws IOException {
        this.uposMapper = uposMapper;
        this.dictionary = new RAMDictionary(wordnetFolder, ILoadPolicy.IMMEDIATE_LOAD);
        dictionary.open();
    }

    @Override
    public String[] getSynsetIds(String[] lemmas, String[] poses, Language language) {
        return getSynsetIds(lemmas, poses);
    }

    @Override
    public String getSynsetId(String lemma, String pos, Language language) {
        return getSynsetId(lemma, pos);
    }

    @Override
    public String[] getSynsetIds(String[] lemmas, String[] poses) {
        String[] synsetIds = new String[lemmas.length];
        for(int i = 0; i < lemmas.length; i ++){
            synsetIds[i] = getSynsetId(lemmas[i], poses[i]);
        }
        return synsetIds;
    }

    @Override
    public synchronized String getSynsetId(String lemma, String pos) {
        String simplePOS = uposMapper.convert(pos);
        if(simplePOS == null) return lemma;
        IIndexWord indexWord = this.dictionary.getIndexWord(lemma, POS.valueOf(simplePOS));
        if(indexWord == null) return lemma;
        List<IWordID> wordIDS = indexWord.getWordIDs();
        if(wordIDS.isEmpty()) return lemma;
        return wordIDS
                .stream()
                .max(Comparator
                        .comparing(id->dictionary.getSenseEntry(
                                dictionary.getWord(id).getSenseKey()).getTagCount()))
                .get()
                .getSynsetID()
                .toString()
                .substring(4)
                .toLowerCase();
    }

    public void close(){
        dictionary.close();
    }
}
