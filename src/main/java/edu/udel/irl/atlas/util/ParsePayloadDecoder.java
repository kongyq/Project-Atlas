package edu.udel.irl.atlas.util;

import org.apache.lucene.util.BytesRef;

public class ParsePayloadDecoder {

    public static int getShortestPath(byte[] tokenCode1, byte[] tokenCode2){
        int shortestPath = tokenCode1.length + tokenCode2.length;
        int shorterCode = Math.min(tokenCode1.length, tokenCode2.length);
        int index = 0;
        while(index < shorterCode && tokenCode1[index] == tokenCode2[index]){
            shortestPath -= 2;
            index++;
        }
        return shortestPath;
    }

    public static int getShortestPath(BytesRef parseCode1, BytesRef parseCode2){
        return getShortestPath(parseCode1.bytes, parseCode2.bytes);
    }
}
