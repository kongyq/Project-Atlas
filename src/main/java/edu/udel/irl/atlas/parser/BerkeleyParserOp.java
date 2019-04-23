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

//TODO: This parser is not stable for mass document processing. Do not use it!!

/**
 * An Berkeley parser extends {@link ParserOp}.
 * This will share one parser model among all threads,
 * but create instance for each thread to make the parser
 * thread-safe.
 */
public class BerkeleyParserOp extends ParserOp{
    // Parse model shared among all threads
    private final ParserData parserData;

    public BerkeleyParserOp(){
        this.parserData = null;
    }

    public BerkeleyParserOp(File modelFile){
        this(ParserData.Load(modelFile.getAbsolutePath()));
    }

    public BerkeleyParserOp(ParserData parserData){
        this.parserData = parserData;
        Numberer.setNumberers(this.parserData.getNumbs());
    }

    /**Create a instance of {@link OpParser } which extends {@link ParserOp.OpParser}
     * for {@link edu.udel.irl.atlas.analysis.ParsePayloadFilter} to parse the {@link org.apache.lucene.analysis.TokenStream}
     * @return a {@link OpParser} instance
     */
    @Override
    public OpParser createParser(){
        return new OpParser(new CoarseToFineMaxRuleParser(
                this.parserData.getGrammar(),
                this.parserData.getLexicon(),
                1d,
                -1,
                false, false, false,
                true, false, false,
                true
        ));
    }

    public class OpParser extends ParserOp.OpParser<CoarseToFineMaxRuleParser>{

        public Tree<String> parse;

        OpParser(CoarseToFineMaxRuleParser parser) {
            super(parser);
        }

        @Override
        public String[] getSentence() {
            return this.parse.getTerminalYield().toArray(new String[0]);
        }

        @Override
        public void parseSent(String[] sentence) {
            this.parse = TreeAnnotations.unAnnotateTree(
                    parser.getBestParse(Arrays.asList(sentence)),
                    false);
        }

        @Override
        public List<byte[]> getCodeList() {
            return ParsePayloadEncoder.encode(parse);
        }

        @Override
        public List<byte[]> getCodeList(short sentNum) {
            return ParsePayloadEncoder.encode(parse, sentNum);
        }

        @Override
        public String[] getPosTags() {
            return parse.getPreTerminals().stream().map(Tree::getLabel).toArray(String[]::new);
        }
    }
}
