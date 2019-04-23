package edu.udel.irl.atlas.util;

import edu.berkeley.nlp.syntax.Tree;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParsePayloadEncoder {

    public static <L> List<byte[]> encode(Tree<L> parse) {
        List<byte[]> codeList = new ArrayList<>();
        encode(codeList, parse, new byte[0]);
        return codeList;
    }

    public static List<byte[]> encode(Parse parse) {
        List<byte[]> codeList = new ArrayList<>();
        encode(codeList, parse, new byte[0]);
        return codeList;
    }

    /**
     * <P>Encode each token in the parsing tree with their corresponding positions.</P>
     *<B>NOTE:</B> if any constituency part of the sentence contains more then 256 terminal words will fail to code.
     * since one byte can only contain unsigned 256 numbers.
     * In order to not interrupt the indexing process, any word beyond 256 will has the same code.
     * @param codeList Encoded List
     * @param parse    Parsing tree
     * @param levels   Height of the starting point
     */
    private static void encode(List<byte[]> codeList, Parse parse, byte[] levels) {
        if (parse.getType().equals(AbstractBottomUpParser.TOK_NODE))
            codeList.add(levels);
        else {
            Parse[] children = parse.getChildren();
//            if(children.length <= 256) System.out.println("Error, the words in the same sentence level are over 256!!");
            for (int i = 0; i < children.length; i++) {  // Used unsigned byte to store the position, thus it can code 256 tokens maximum.
                byte[] nextLevels = Arrays.copyOf(levels, levels.length + 1);
                nextLevels[levels.length] = (byte) Math.min(i, 255);
                encode(codeList, children[i], nextLevels);
            }
        }
    }

    /**
     * <P>Encode each token in the parsing tree with their corresponding positions.</P>
     *<B>NOTE:</B> if any constituency part of the sentence contains more then 256 terminal words will fail to code.
     * since one byte can only contain 256 unsigned numbers.
     * In order to not interrupt the indexing process, any word beyond 256 will has the same code.
     * @param codeList Encoded List
     * @param parse    Parsing tree
     * @param levels   Height of the starting point
     */
    private static <L> void encode(List<byte[]> codeList, Tree<L> parse, byte[] levels) {
        if (parse.isLeaf())
            codeList.add(levels);
        else {
            List<Tree<L>> children = parse.getChildren();
//            if(children.size() <= 256) System.out.println("Error, the words in the same sentence level are over 256!!");
            for (int i = 0; i < children.size(); i++) {
                byte[] nextLevels = Arrays.copyOf(levels, levels.length + 1);
                nextLevels[levels.length] = (byte) Math.min(i, 255);
                encode(codeList, children.get(i), nextLevels);
            }
        }
    }

    public static List<byte[]> encode(Parse parse, short sentNum) {
        List<byte[]> codeList = new ArrayList<>();
        encode(codeList, parse, Short2Bytes.encodeShort(sentNum));
        return codeList;
    }

    public static <L> List<byte[]> encode(Tree<L> parse, short sentNum) {
        List<byte[]> codeList = new ArrayList<>();
        encode(codeList, parse, Short2Bytes.encodeShort(sentNum));
        return codeList;
    }
}