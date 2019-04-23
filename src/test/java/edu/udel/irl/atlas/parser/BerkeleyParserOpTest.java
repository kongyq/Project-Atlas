package edu.udel.irl.atlas.parser;

import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.syntax.Tree;
import org.junit.Test;

import static org.junit.Assert.*;

public class BerkeleyParserOpTest {
    private final BerkeleyParserOp parserOp;

    public BerkeleyParserOpTest(){
        this.parserOp = new BerkeleyParserOp(ParserData.Load("/home/mike/Documents/Index/BerkeleyParser/models/eng_sm6.gr"));
    }
    @Test
    public void parseSent() {
        String sentence2 = "the quick brown fox jumps over the lazy dog .";
        String sentence = "Interested persons may obtain further information by contacting the Federal Energy Regulatory Commission , 941 North Capitol Street , NE. , Washington , DC 20426 [Attention : Michael Miller , Information Services Division , ( 202 ) 208&hyph ;1415] .";
        ParserOp.OpParser parser = this.parserOp.createParser();

        parser.parseSent(sentence2.split(" "));
        System.out.println(String.join(" ", parser.getSentence()));
//        ((BerkeleyParserOp.OpParser) parser).parse.toString();
        for(String pos: parser.getPosTags()) System.out.println(pos);
//        for(Tree<String> t: ((BerkeleyParserOp.OpParser) parser).parse) System.out.println(t.getLabel());
//        System.out.println(parser.getParse().toString());
//        for(String pos: this.parserOp.getPosTags()) System.out.println(pos);
//        for(Tree<String> t: parse.getPreTerminals()){
//            System.out.println(t.getLabel());
//        }
    }

    @Test
    public void parseSent1() {
//        String sentence = "The quick brown fox jumps over the lazy dog";
//        String poses = "DT JJ JJ NN VBZ IN DT JJ NN";
//        Tree<String> parse = this.parserOp.parseSent(sentence.split(" "), poses.split(" "));
//        System.out.println(parse.toEscapedString());
    }
}