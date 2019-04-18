package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.util.Short2Bytes;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FutureArrays;

import java.util.Arrays;

/**
 * A data structure stores bar data, which includes sentence number and parsing codes for both doc side and query side.
 * and also similarity between two terminal nodes of doc side and query side.
 * <P></P>
 * <p>  bar1|bar2
 * <p>     / \ -> doc side codes(encoded parsing tree) and header(sentence index)
 * <p>     | | -> weight (synset similarity)
 * <p>     \ / -> query side codes and header
 * <p>   a cycle
 */
public class AtlasBar implements Comparable<AtlasBar> {

    public final float weight;
    public final byte[] docEnd;
    public final byte[] queryEnd;
    public final short docheader;
    public final short queryheader;

    public AtlasBar(float weight, BytesRef docPayload, BytesRef queryPayload){
        this.weight = weight;
        this.docEnd = decode(docPayload);
        this.queryEnd = decode(queryPayload);
        this.docheader = Short2Bytes.decodeShort(docEnd);
        this.queryheader = Short2Bytes.decodeShort(queryEnd);
    }

    private byte[] decode(BytesRef payload){
        return Arrays.copyOfRange(payload.bytes, payload.offset, payload.offset + payload.length);
    }

    /**
     * compare two AtlasBars. compare header first to make sure sentences are aligned,
     * then compare similarity between the terms of doc and query. higher similarity
     * goes first, finally compare the query codes and doc codes. first appear in the
     * sentence goes first.
     * @param other comparee
     */
    @Override
    public int compareTo(AtlasBar other) {
        int queryHeaderCompare = Short.toUnsignedInt(this.queryheader) - Short.toUnsignedInt(other.queryheader);
        if(queryHeaderCompare == 0){
            int docHeaderCompare = Short.toUnsignedInt(this.docheader) - Short.toUnsignedInt(other.docheader);
            if(docHeaderCompare == 0){
                int weightCompare = Float.compare(this.weight, other.weight);
                if(weightCompare == 0){
                    int queryCodeCompare = FutureArrays.compareUnsigned(queryEnd, 2, queryEnd.length, other.queryEnd, 2, other.queryEnd.length);
                    if(queryCodeCompare == 0){
                        return FutureArrays.compareUnsigned(docEnd, 2, docEnd.length, other.docEnd, 2, other.docEnd.length);
                    }else {
                        return queryCodeCompare;
                    }
                }else {
                    return -weightCompare;
                }
            }else {
                return docHeaderCompare;
            }
        }else {
            return queryHeaderCompare;
        }
    }
}
