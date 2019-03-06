package edu.udel.irl.atlas.babelnet;

import com.babelscape.util.UniversalPOS;
import edu.udel.irl.atlas.util.UPOSMapper;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.jlt.util.Language;

import java.util.List;

/***
 * <P>Supply BabelNet most common synset ID for the lemma and POS.</P>
 * <P>
 *     BableNet online or offline index need to be correctly configured in babelnet.properties file.
 * </P>
 * <P>
 *     Since the treebank pos tag need to be convert into universal pos tag,
 *     an UPOSmapper instance need to be pass to the constructor.
 * </P>
 */
public class BNSynsetOp implements SynsetOp{
    private final BabelNet babelNet;
    private final UPOSMapper uposMapper;

    public BNSynsetOp(UPOSMapper uposMapper){
        this.uposMapper = uposMapper;
        this.babelNet = BabelNet.getInstance();
    }

    public BNSynsetOp(){
        this.uposMapper = null;
        this.babelNet = null;
//        this(new UPOSMapper("en-bn"));
    }

    @Override
    public String getSynsetId(String lemma, String pos, Language language){
        String universalPOS = uposMapper.convert(pos);
        if(universalPOS == null) return lemma;
        return getSynsetId(lemma, UniversalPOS.valueOf(universalPOS), language);
    }

    public synchronized String getSynsetId(String lemma, UniversalPOS pos, Language language){
        List<BabelSynset> synsets = babelNet.getSynsets(
                (new BabelNetQuery.Builder(lemma))
                        .from(language)
                        .POS(pos)
                        .build());
        if(synsets.isEmpty()) return lemma;
        synsets.sort(new BabelSynsetComparator(lemma));
        return synsetToString(synsets.get(0), lemma);
    }

    public String synsetToString(BabelSynset synset, String lemma){
        return synset.getID().toString();
    }

    @Override
    public String[] getSynsetIds(String[] lemmas, String[] poses, Language language){
        String[] synsetIds = new String[lemmas.length];
        for(int i = 0; i < lemmas.length; i ++){
            synsetIds[i] = getSynsetId(lemmas[i], poses[i], language);
        }
        return synsetIds;
    }

    @Override
    public String getSynsetId(String lemma, String pos){
        return getSynsetId(lemma, pos, Language.EN);
    }

    @Override
    public String[] getSynsetIds(String[] lemmas, String[] poses){
        return getSynsetIds(lemmas, poses, Language.EN);
    }
}
