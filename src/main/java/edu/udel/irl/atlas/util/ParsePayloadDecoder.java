package edu.udel.irl.atlas.util;

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
}
