package edu.udel.irl.atlas.synsim;

import org.junit.Test;

import static org.junit.Assert.*;

public class ADWSynsetSimilarityTest {

    @Test
    public void compare() {
//        System.out.println(ADWSynsetSimilarity.getInstance().compare("00381097-a", "00385756-a")); // yellow vs red
        System.out.println(ADWSynsetSimilarity.getInstance().compare("P.F.Changs", "P.F.Changs")); // yellow vs red
    }
}