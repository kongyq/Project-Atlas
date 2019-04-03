package edu.udel.irl.atlas.babelnet;

import com.babelscape.util.UniversalPOS;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import edu.udel.irl.atlas.util.UPOSMapper;
import it.uniroma1.lcl.jlt.util.Language;
import org.junit.Test;

import static org.junit.Assert.*;

public class BNSynsetOpTest {

    private SynsetOp bnSynsetOp = new BNSynsetOp(new UPOSMapper(AtlasConfiguration.getInstance().getPOSMapperFolder() + "/en-bn.map"));
    public BNSynsetOpTest(){

    }
    @Test
    public void getSynsetId() {
        System.out.println(bnSynsetOp.getSynsetId("apple", "NN", Language.EN));
    }

    @Test
    public void getSynsetIds() {
        String[] sent = "Fruit with red or yellow or green skin and sweet to tart crisp whitish flesh".split(" ");
        String[] poses = "NNP IN JJ CC JJ CC JJ NN CC JJ TO JJ JJ JJ NN".split(" ");
        String[] res = bnSynsetOp.getSynsetIds(sent, poses, Language.EN);
        for(String word: res) System.out.println(word);
    }
}