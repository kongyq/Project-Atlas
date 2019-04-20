package edu.udel.irl.atlas.util;

import edu.udel.irl.atlas.parser.NLPParserOp;
import edu.udel.irl.atlas.parser.ParserOp;
import opennlp.tools.parser.ParserModel;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class ParsePayloadDecoderTest {
    private final String sentence = "the quick brown fox jumps over the lazy dog .";
    private ParserOp parserOp;
    public ParsePayloadDecoderTest() throws IOException {
        this.parserOp = new NLPParserOp(new ParserModel(new FileInputStream("/home/mike/Documents/Index/OpenNLP/models/en-parser-chunking.bin")));
    }
    @Test
    public void getShortestPath() {
//        parserOp.parseSent(this.sentence.split(" "));
//        List<byte[]> codeList = parserOp.getCodeList();
//        System.out.println(ParsePayloadDecoder.getShortestPath(codeList.get(0), codeList.get(1)));
    }
}