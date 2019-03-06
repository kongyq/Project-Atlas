package edu.udel.irl.atlas.babelnet;

import edu.udel.irl.atlas.util.UPOSMapper;
import it.uniroma1.lcl.jlt.util.Language;
import org.junit.Test;

import static org.junit.Assert.*;

public class BNWNSynsetOpTest {
    private SynsetOp synsetOp = new BNWNSynsetOp(new UPOSMapper("en-bn"));
    @Test
    public void synsetToString() {
        System.out.println(synsetOp.getSynsetId("apple", "NN"));
    }

    @Test
    public void getSynsetIds() {
        String[] sent = "Fruit with red or yellow or green skin and sweet to tart crisp whitish flesh".split(" ");
        String[] poses = "NNP IN JJ CC JJ CC JJ NN CC JJ TO JJ JJ JJ NN".split(" ");
        String[] res = synsetOp.getSynsetIds(sent, poses);
        for(String word: res) System.out.println(word);
    }
}