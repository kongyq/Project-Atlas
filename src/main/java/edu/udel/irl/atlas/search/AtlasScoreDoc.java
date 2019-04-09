package edu.udel.irl.atlas.search;

import org.apache.lucene.search.ScoreDoc;

import java.util.Arrays;

public class AtlasScoreDoc implements Comparable<AtlasScoreDoc> {

    // Document ID
    public String docId;

    // The final Score associates with this document
    public float score;

    // All scored sentences associate with this docId
    public ScoreDoc[] scoreDocs;

    public AtlasScoreDoc(String docId, ScoreDoc[] scoreDocs){
        this.docId = docId;
        this.scoreDocs = scoreDocs;
        this.score = (float) Arrays.stream(scoreDocs).mapToDouble(scoreDoc -> scoreDoc.score).sum();
    }

    // A convenience method for debugging.
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("docId=").append(docId).append(" score=").append(score).append("\n");
        Arrays.stream(scoreDocs).forEach(ScoreDoc -> {
            builder.append("\t");
            builder.append(ScoreDoc.toString());
            builder.append("\n");
        });
        return builder.toString();
    }

    @Override
    public int compareTo(AtlasScoreDoc other) {
        return -Float.compare(this.score, other.score);
    }
}
