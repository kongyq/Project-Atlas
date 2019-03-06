package edu.udel.irl.atlas.babelnet;

import edu.udel.irl.atlas.util.UPOSMapper;
import it.uniroma1.lcl.jlt.util.Language;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class JWISynsetOpTest {

    private SynsetOp synsetOp = new JWISynsetOp(new UPOSMapper("en-wn"));
//            new File("/home/mike/Documents/Index/WordNet-3.0/dict"));

    public JWISynsetOpTest() throws IOException {
    }

    @Test
    public void getSynsetIds() {
        String[] sent = "Fruit with red or yellow or green skin and sweet to tart crisp whitish flesh".split(" ");
        String[] poses = "NNP IN JJ CC JJ CC JJ NN CC JJ TO JJ JJ JJ NN".split(" ");
        String[] res = synsetOp.getSynsetIds(sent, poses);
        for(String word: res) System.out.println(word);
    }

    @Test
    public void getSynsetId() {
        System.out.println(synsetOp.getSynsetId("apple", "NN"));
    }
}