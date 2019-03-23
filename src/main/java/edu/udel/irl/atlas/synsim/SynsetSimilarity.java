package edu.udel.irl.atlas.synsim;

import java.util.List;

public abstract class SynsetSimilarity {

    public abstract double compare(String synsetId1, String synsetId2);

    public double compare(List<String> synsetList1, List<String> synsetList2){
        double score = 0D;
        for(String synset1: synsetList1){
            for(String synset2: synsetList2){
                double subScore = this.compare(synset1, synset2);
                if(subScore > score){score = subScore;}
            }
        }
        return (score > 1D)? 1D: score;
    }
}
