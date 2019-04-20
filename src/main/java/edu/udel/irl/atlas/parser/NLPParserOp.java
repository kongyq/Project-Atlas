package edu.udel.irl.atlas.parser;

import edu.udel.irl.atlas.util.ParsePayloadEncoder;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * An openNLP parser extends {@link ParserOp}.
 * This will share one parser model among all threads,
 * but create instance for each thread to make the parser
 * thread-safe.
 */
public class NLPParserOp extends ParserOp{
    // Parse model shared among all threads
    private final ParserModel model;

    public NLPParserOp(){
        this.model = null;
    }

    public NLPParserOp(File modelFile) throws IOException {
        this(new ParserModel(modelFile));
    }
    public NLPParserOp(ParserModel parserModel){
        this.model = parserModel;
    }

    /**Create a instance of {@link OpParser } which extends {@link ParserOp.OpParser}
     * for {@link edu.udel.irl.atlas.analysis.ParsePayloadFilter} to parse the {@link org.apache.lucene.analysis.TokenStream}
     * @return a {@link OpParser} instance
     */
    @Override
    public OpParser createParser(){
        return new OpParser(ParserFactory.create(this.model));
    }

    public class OpParser extends ParserOp.OpParser<Parser>{

        public Parse parse;

        OpParser(Parser parser) {
            super(parser);
        }

        @Override
        public void parseSent(String[] sentence) {
            this.parse = ParserTool.parseLine(String.join(" ", sentence), parser, 1)[0];
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
            return Arrays.stream(parse.getTagNodes()).map(Parse::getType).toArray(String[]::new);
        }
    }
}
