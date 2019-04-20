package edu.udel.irl.atlas.parser;

import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserModel;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class NLPParserOpTest {

    private final NLPParserOp parserOp;

    public NLPParserOpTest() throws IOException {
        this.parserOp = new NLPParserOp(new ParserModel(new FileInputStream("/home/mike/Documents/Index/OpenNLP/models/en-parser-chunking.bin")));
    }

    @Test
    public void parseSent() {
        String sentence = "the quick brown fox jumps over the lazy dog .";
        ParserOp.OpParser parser = this.parserOp.createParser();
        parser.parseSent(sentence.split(" "));
        ((NLPParserOp.OpParser) parser).parse.show();
    }


    @Test
    public void parseSent1() {
//        String sentence = "The quick brown fox jumps over the lazy dog .";
//        parserOp.parseSent(sentence.split(" "));
//        for(String pos: this.parserOp.getPosTags()) System.out.println(pos);
        System.out.println(ClassLoader.getSystemResource(""));

    }
}