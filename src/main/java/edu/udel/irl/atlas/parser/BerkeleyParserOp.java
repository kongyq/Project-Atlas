package edu.udel.irl.atlas.parser;

import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.PCFGLA.TreeAnnotations;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.Numberer;
import edu.udel.irl.atlas.util.ParsePayloadEncoder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class BerkeleyParserOp implements ParserOp<Tree<String>>{
    private final CoarseToFineMaxRuleParser parser;
    private Tree<String> parse;

    public BerkeleyParserOp(){
        this.parser = null;
    }

    public BerkeleyParserOp(File modelFile){
        this(ParserData.Load(modelFile.getAbsolutePath()));
    }

    public BerkeleyParserOp(ParserData parserData){
        Numberer.setNumberers(parserData.getNumbs());
        this.parser = new CoarseToFineMaxRuleParser(
                parserData.getGrammar(),
                parserData.getLexicon(),
                1.0,
                -1,
                false, false, false,false, false,false,true);
        this.parser.binarization = parserData.getBinarization();
    }

    @Override
    public synchronized void parseSent(String[] sentence){
        this.parse = TreeAnnotations.unAnnotateTree(
                this.parser.getBestConstrainedParse(
                        Arrays.asList(sentence),
                        null,
                        false),
                false);
    }

    @Override
    public String[] getPosTags() {
        return this.parse.getPreTerminals().stream().map(Tree::getLabel).toArray(String[]::new);
    }

    @Override
    public Tree<String> getParse() {
        return this.parse;
    }

    @Override
    public List<byte[]> getCodeList() {
        return ParsePayloadEncoder.encode(this.parse);
    }

    @Deprecated
    public Tree<String> parseSent(String[] sentence, String[] poses){
        return TreeAnnotations.unAnnotateTree(
                this.parser.getBestConstrainedParse(
                        Arrays.asList(sentence),
                        Arrays.asList(poses),
                        null),
                false);
    }
}
