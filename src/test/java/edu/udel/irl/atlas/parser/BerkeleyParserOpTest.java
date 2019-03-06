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

        String sentence = "The quick brown fox jumps over the lazy dog .";
        this.parserOp.parseSent(sentence.split(" "));
        System.out.println(this.parserOp.getParse().toString());
        for(String pos: this.parserOp.getPosTags()) System.out.println(pos);
//        for(Tree<String> t: parse.getPreTerminals()){
//            System.out.println(t.getLabel());
//        }
    }

    @Test
    public void parseSent1() {
        String sentence = "The quick brown fox jumps over the lazy dog";
        String poses = "DT JJ JJ NN VBZ IN DT JJ NN";
        Tree<String> parse = this.parserOp.parseSent(sentence.split(" "), poses.split(" "));
        System.out.println(parse.toEscapedString());
    }
}