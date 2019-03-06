package edu.udel.irl.atlas.parser;

import edu.udel.irl.atlas.util.ParsePayloadEncoder;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import java.util.Arrays;
import java.util.List;

public class NLPParserOp implements ParserOp<Parse>{
    private final Parser parser;
    private Parse parse;

    public NLPParserOp(){
        this.parser = null;
    }

    public NLPParserOp(ParserModel parserModel){
        this.parser = ParserFactory.create(parserModel);
    }

    @Override
    public String[] getPosTags() {
        return Arrays.stream(this.parse.getTagNodes()).map(Parse::getType).toArray(String[]::new);
    }

    @Override
    public Parse getParse() {
        return this.parse;
    }

    @Override
    public List<byte[]> getCodeList() {
        return ParsePayloadEncoder.encode(this.parse);
    }

    @Override
    public synchronized void parseSent(String[] sentence){
        this.parse = parseSent(String.join(" ", sentence));
    }

    public synchronized Parse parseSent(String sentence){
        return ParserTool.parseLine(sentence, this.parser, 1)[0];
    }
}
