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
     *
     * @param codeList Encoded List
     * @param parse    Parsing tree
     * @param levels   Height of the starting point
     */
    private static void encode(List<byte[]> codeList, Parse parse, byte[] levels) {
        if (parse.getType().equals(AbstractBottomUpParser.TOK_NODE))
            codeList.add(levels);
        else {
            Parse[] children = parse.getChildren();
            assert children.length <= 256 : "Error, the words in the same sentence level are over 256!!";
            for (int i = 0; i < Math.min(children.length, 256); i++) {  // Used unsigned byte to store the position, thus it can code 256 tokens maximum.
                byte[] nextLevels = Arrays.copyOf(levels, levels.length + 1);
                nextLevels[levels.length] = (byte) i;
                encode(codeList, children[i], nextLevels);
            }
        }
    }

    /**
     * <P>Encode each token in the parsing tree with their corresponding positions.</P>
     *
     * @param codeList Encoded List
     * @param parse    Parsing tree
     * @param levels   Height of the starting point
     */
    private static <L> void encode(List<byte[]> codeList, Tree<L> parse, byte[] levels) {
        if (parse.isLeaf())
            codeList.add(levels);
        else {
            List<Tree<L>> children = parse.getChildren();
            assert children.size() <= 256 : "Error, the words in the same sentence level are over 256!!";
            for (int i = 0; i < Math.min(children.size(), 256); i++) {
                byte[] nextLevels = Arrays.copyOf(levels, levels.length + 1);
                nextLevels[levels.length] = (byte) i;
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