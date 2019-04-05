package edu.udel.irl.atlas.search.function;

import edu.udel.irl.atlas.util.ParsePayloadDecoder;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.util.BytesRef;

import java.util.*;

public class AtlasScoreFunction extends ScoreFunction{

    private List<Explanation> cycles = new ArrayList<>();

    @Override
    public float docScore(int docId, String field, List<Term> termList, List<BytesRef> payloadList, Map<Term, ? extends Map<BytesRef, Float>> queryMap) {

        cycles.clear();
//        termList.forEach(System.out::println);
//        payloadList.forEach(System.out::println);

        System.out.println("Call docScore func!! inside");
        int matchingLength = termList.size();
        int totalEdges = termList.stream().map(queryMap::get).mapToInt(Map::size).sum();
        // no cycle formed
        if (totalEdges < 2) return 0f;

        Map<BytesRef[], Float> bars = new HashMap<>(totalEdges);
//        Object2FloatMap<BytesRef> brats = new Object2FloatOpenHashMap<>(totalEdges);

        for(int i = 0; i < matchingLength; i ++){
            for(Map.Entry<BytesRef, Float> entry : queryMap.get(termList.get(i)).entrySet()){
                BytesRef[] nodes = new BytesRef[2];
                nodes[0] = payloadList.get(i);
                nodes[1] = entry.getKey();
                bars.put(nodes, entry.getValue());
            }
        }
        float score = 0f;
        List<Map.Entry<BytesRef[], Float>> barList = new ArrayList<>(bars.entrySet());
        boolean needsSort = true;

        if(needsSort) {
            barList.sort(Comparator.comparing(Map.Entry<BytesRef[], Float>::getValue).reversed());
        }

        float finalScore = 0f;
        for(int i = 0; i < barList.size()-1; i ++){
            System.out.println("Call cycleScore func!!");
            finalScore += cycleScore(barList.get(i), barList.get(i + 1));
        }
        return finalScore;
    }

    /**
     * A harmonic mean function to calculate the cycle score
     * @param first the first doc-query arc of the cycle
     * @param second the second doc-query arc of the cycle
     * @return the cycle score
     */
    private float cycleScore(Map.Entry<BytesRef[], Float> first, Map.Entry<BytesRef[], Float> second){
        // cycle coefficient of the intra-tree weights | |
        float sai = Math.min(first.getValue(), second.getValue());
        // distance between two terminals in document arc /\
        int distInDocArc = ParsePayloadDecoder.getShortestPath(decodeBytesRef(first.getKey()[0]), decodeBytesRef(second.getKey()[0]));
        if(distInDocArc == 0) distInDocArc = 1;
        // distance between two terminals in query arc \/
        int distInQueryArc = ParsePayloadDecoder.getShortestPath(first.getKey()[1], second.getKey()[1]);
        if(distInQueryArc == 0) distInQueryArc = 1;
        // harmonic mean W_ci = exp(1 / distInDocArc ^ 3 + distInQueryArc ^ 3)
        float harmonicMean = (float) Math.exp(1D / (Math.pow(distInDocArc, 3) + Math.pow(distInQueryArc, 3)));

        cycles.add(Explanation.match(sai * harmonicMean, "Cycle score, computed as Sai * Exp(1 / (Arc_d ^ 3 + Arc_q ^ 3))",
                Explanation.match(sai, "coefficient Sai: min(bar1, bar2)",
                        Explanation.match(first.getValue(), "bar1"),
                        Explanation.match(second.getValue(), "bar2")),
                Explanation.match(harmonicMean, "harmonic mean",
                        Explanation.match(distInDocArc, "Arc_d: distance of two terminals in doc arc"),
                        Explanation.match(distInQueryArc, "Arc_q: distance of two terminals in query arc"))));

        // final cycle score W = sai * W_ci
        return sai * harmonicMean;
    }

    /**
     * decode the payload from BytesRef since the indexing procedure will reformat the payload with paddings.
     * @param bytesRef BytesRef need to be reformat into byte[]
     * @return byte[] contains payload
     */
    private static byte[] decodeBytesRef(BytesRef bytesRef){
        return Arrays.copyOfRange(bytesRef.bytes, bytesRef.offset, bytesRef.offset + bytesRef.length);
    }

    @Override
    public Explanation explain(int docId, String field, List<Term> termList, List<BytesRef> payloadList, Map<Term, ? extends Map<BytesRef, Float>> queryMap){
        System.out.println("Call docScore func!");
        final float finalScore = docScore(docId, field, termList, payloadList, queryMap);
        return Explanation.match(finalScore, "AtlasScore, computed as sum of cycle scores", cycles);
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
