package edu.udel.irl.atlas.search;

import java.util.Arrays;

/**
 * A modified TopDocs class stores top hits and matching documents.
 */
@Deprecated
public class AtlasTopDocs {

    public AtlasScoreDoc[] scoreDocs;

    public long totalHits;

    public AtlasTopDocs(long totalHits, AtlasScoreDoc[] scoreDocs){
        this.totalHits = totalHits;
        this.scoreDocs = scoreDocs;
    }

    /**
     * A static method creates a instance of AtlasTopDocs.
     * This will compare and sort AtlasScoreDoc array based
     * on its score field.
     *
     * @param totalHits the number of document
     * @param scoreDocs the matching document array
     * @return a AtlasTopDocs instance
     */
    public static AtlasTopDocs create(long totalHits, AtlasScoreDoc[] scoreDocs){
        Arrays.sort(scoreDocs);
        int availableHits = (int) Math.min(totalHits, scoreDocs.length);
        return new AtlasTopDocs(availableHits, Arrays.copyOf(scoreDocs, availableHits));
    }
}
