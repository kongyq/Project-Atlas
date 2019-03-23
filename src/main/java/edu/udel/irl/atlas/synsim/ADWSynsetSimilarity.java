package edu.udel.irl.atlas.synsim;

import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;

/**
 * Created by mike on 4/18/18.
 * Modified to singleton on 6/1/18.
 */
public class ADWSynsetSimilarity extends SynsetSimilarity {

    private static ADWSynsetSimilarity instance = null;

    private ADW pipeline;
    private SignatureComparison measure;

    private ADWSynsetSimilarity(){
        this.pipeline = new ADW();
        this.measure = new WeightedOverlap();
    }

    @Override
    public double compare(String synset1, String synset2){
        return this.pipeline.getPairSimilarity(
                synset1, synset2,
                DisambiguationMethod.ALIGNMENT_BASED,
                this.measure,
                ItemType.SENSE_OFFSETS, ItemType.SENSE_OFFSETS);
    }

    public static synchronized ADWSynsetSimilarity getInstance(){
        if (instance == null){
            instance = new ADWSynsetSimilarity();
        }
        return instance;
    }
}

