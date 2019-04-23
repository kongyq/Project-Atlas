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
        String test = "2100 ( 950 ) 3/8 ( 10 ) 3000 ( 1360 ) 7/16 ( 11 ) 4100 ( 1860 ) 1/2 ( 13 ) 5300 ( 2400 ) 5/8 ( 16 ) 8300 ( 3770 ) 3/4 ( 20 ) 10900 ( 4940 ) 7/8 ( 22 ) 16100 ( 7300 ) 1 ( 25 ) 20900 ( 9480 ) s ,s 1 Manila Rope WLL 0 s ,s 3/8 ( 10 ) 205 (90 ) 7/16 ( 11 ) 265 ( 120 ) 1/2 ( 13 ) 315 ( 150 ) 5/8 ( 16 ) 465 ( 210 ) 3/4 ( 20 ) 640 ( 290 ) 1 ( 25 ) 1050 ( 480 ) s ,s 1 Polypropylene Fiber Rope WLL (3-Strand and 8-Strand Constructions ) 0 s ,s 3/8 ( 10 ) 400 ( 180 ) 7/16 ( 11 ) 525 ( 240 ) 1/2 ( 13 ) 625 ( 280 ) 5/8 ( 16 ) 925 ( 420 ) 3/4 ( 20 ) 1275 ( 580 ) 1 ( 25 ) 2100 ( 950 ) s ,s 1 Polyester Fiber Rope WLL (3-Strand and 8-Strand Constructions ) 0 s ,s 3/8 ( 10 ) 555 ( 250 ) 7/16 ( 11 ) 750 ( 340 ) 1/2 ( 13 ) 960 ( 440 ) 5/8 ( 16 ) 1500 ( 680 ) 3/4 ( 20 ) 1880 ( 850 ) 1 ( 25 ) 3300 ( 1500 ) s ,s 1 Nylon Rope WLL 0 s ,s 3/8 ( 10 ) 278 ( 130 ) 7/16 ( 11 ) 410 ( 190 ) 1/2 ( 13 ) 525 ( 240 ) 5/8 ( 16 ) 935 ( 420 ) 3/4 ( 20 ) 1420 ( 640 ) 1 ( 25 ) 2520 ( 1140 ) s ,s";
        String test2 = "FR940706-0-00014 Tables to §393.102( b )( 6 ) _";
        ParserOp.OpParser parser = this.parserOp.createParser();
        parser.parseSent(test2.split(" "));
        ((NLPParserOp.OpParser) parser).parse.showCodeTree();
        System.out.println(String.join(" ", parser.getPosTags()));
        System.out.println(((NLPParserOp.OpParser) parser).getCodeList().size());
        System.out.println(String.join(" ", parser.getSentence()));
    }


    @Test
    public void parseSent1() {
//        String sentence = "The quick brown fox jumps over the lazy dog .";
//        parserOp.parseSent(sentence.split(" "));
//        for(String pos: this.parserOp.getPosTags()) System.out.println(pos);
        System.out.println(ClassLoader.getSystemResource(""));

    }
}