package edu.udel.irl.atlas.babelnet;

import edu.udel.irl.atlas.util.UPOSMapper;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.WordNetSynsetID;

import java.util.List;

public class BNWNSynsetOp extends BNSynsetOp implements SynsetOp{
    public BNWNSynsetOp(UPOSMapper uposMapper) {
        super(uposMapper);
    }

    public BNWNSynsetOp(){
        super(null);
//        this(new UPOSMapper("en-bn"));
        }

    @Override
    public String synsetToString(BabelSynset synset, String lemma){
        List<WordNetSynsetID> synsetIDS = synset.getWordNetOffsets();
        if(synsetIDS.isEmpty()) return lemma;
        return synsetIDS.get(0).toString();
    }
}
