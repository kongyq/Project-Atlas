package edu.udel.irl.atlas.search.function;

import edu.udel.irl.atlas.search.AtlasBar;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import edu.udel.irl.atlas.util.ParsePayloadDecoder;
import org.apache.lucene.search.Explanation;

import java.util.*;

public class AtlasScoreFunction extends ScoreFunction{
    private final static float EXPONENT = AtlasConfiguration.getInstance().getHarmonicMeanExponent();

    private Map<String, List<Explanation>> sentPairs = new HashMap<>();

    @Override
    public float docScore(int docId, String field, List<AtlasBar> bars) {
        //Critical! bars have to be sorted before computing the score of cycles
        Collections.sort(bars);
        sentPairs.clear();

        float finalScore = 0f;

        for(int i = 0; i < bars.size()-1; i ++){
            AtlasBar bar1 = bars.get(i);
            AtlasBar bar2 = bars.get(i + 1);
//            if(bar1.queryheader == bar2.queryheader && bar1.docheader == bar2.docheader) {
                String sentKey = Short.toUnsignedInt(bar1.queryheader) + "<->" + Short.toUnsignedInt(bar1.docheader);
                sentPairs.putIfAbsent(sentKey, new ArrayList<>());
                Explanation cycle = cycleScore(bar1, bar2);
                sentPairs.get(sentKey).add(cycle);
                finalScore += cycle.getValue();
//            }
        }
        return finalScore;
    }

    /**
     * A harmonic mean function to calculate the cycle score
     * @param first the first doc-query arc of the cycle
     * @param second the second doc-query arc of the cycle
     * @return the cycle score
     */
    private Explanation cycleScore(AtlasBar first, AtlasBar second){
        // cycle coefficient of the intra-tree weights | |
        float sai = Math.min(first.weight, second.weight);
//        System.out.println("   Sai -> " + sai);
//        System.out.println("      1 -> " + first.getValue());
//        System.out.println("      2 -> " + second.getValue());
        // distance between two terminals in document arc /\
        int distInDocArc = ParsePayloadDecoder.getShortestPath(first.docEnd, second.docEnd);
        if(distInDocArc == 0) distInDocArc = 1;
        // distance between two terminals in query arc \/
        int distInQueryArc = ParsePayloadDecoder.getShortestPath(first.queryEnd, second.queryEnd);
        if(distInQueryArc == 0) distInQueryArc = 1;
        // harmonic mean W_ci = exp(1 / distInDocArc ^ EXPONENT + distInQueryArc ^ EXPONENT)
        float harmonicMean = (float) Math.exp(1d / (Math.pow(distInDocArc, EXPONENT) + Math.pow(distInQueryArc, EXPONENT)));
//        System.out.println("   H -> " + harmonicMean);
//        System.out.println("      Ad -> " + distInDocArc + ":" + first.getKey()[0].toString() + "|" + second.getKey()[0].toString());
//        System.out.println("      Aq -> " + distInQueryArc + ":" + first.getKey()[1].toString() + "|" + second.getKey()[1].toString());
        return (Explanation.match(sai * harmonicMean, "Cycle score, computed as Sai * Exp(1 / (Arc_d ^ "+ EXPONENT + " + Arc_q ^ " + EXPONENT + "))",
                Explanation.match(sai, "coefficient Sai: min(bar1, bar2)",
                        Explanation.match(first.weight, "bar1"),
                        Explanation.match(second.weight, "bar2")),
                Explanation.match(harmonicMean, "harmonic mean",
                        Explanation.match(distInDocArc, "Arc_d: distance of two terminals in doc arc"),
                        Explanation.match(distInQueryArc, "Arc_q: distance of two terminals in query arc"))));
    }

    @Override
    public Explanation explain(int docId, String field, List<AtlasBar> bars){
        final float finalScore = docScore(docId, field, bars);
        List<Explanation> sentences = new ArrayList<>();
        for(Map.Entry<String, List<Explanation>> entry : sentPairs.entrySet()){
            sentences.add(Explanation.match((float) entry.getValue().stream().mapToDouble(Explanation::getValue).sum(),
                    "Query<->Doc sentence pair score of :" + entry.getKey(), entry.getValue()));
        }
        return Explanation.match(finalScore, "AtlasScore, computed as sum of cycle scores", sentences);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        return getClass() == obj.getClass();
    }
}
