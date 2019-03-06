package edu.udel.irl.atlas.util;

import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.syntax.Tree;
import edu.udel.irl.atlas.parser.BerkeleyParserOp;
import edu.udel.irl.atlas.parser.NLPParserOp;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserModel;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class ParsePayloadEncoderTest {

    private NLPParserOp parserOp;
    private BerkeleyParserOp berkeleyParserOp;

    public ParsePayloadEncoderTest() throws IOException {
        this.parserOp = new NLPParserOp(new ParserModel(new FileInputStream("/home/mike/Documents/Index/OpenNLP/models/en-parser-chunking.bin")));
        this.berkeleyParserOp = new BerkeleyParserOp(ParserData.Load("/home/mike/Documents/Index/BerkeleyParser/models/eng_sm6.gr"));
    }

    @Test
    public void encode() {
        parserOp.parseSent("The quick brown fox jumps over the lazy dog .".split(" "));
        Parse parses = parserOp.getParse();

        List<byte[]> ans = ParsePayloadEncoder.encode(parses);
        for(byte[] codes: ans){
            for(byte code: codes) System.out.print(code);
            System.out.println();
        }
//        parses.showCodeTree();
    }

    @Test
    public void encode1() {
        String sentence = "The quick brown fox jumps over the lazy dog .";
        parserOp.parseSent(sentence.split(" "));
        Tree<String> parse = this.berkeleyParserOp.getParse();
        System.out.println(parse.toString());
        List<byte[]> ans = ParsePayloadEncoder.encode(parse);
        for(byte[] codes: ans){
            for(byte code: codes) System.out.print(code);
            System.out.println();
        }
    }

    @Test
    public void encode2() {
    }
}