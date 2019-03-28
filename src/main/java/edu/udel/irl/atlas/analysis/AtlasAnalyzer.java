package edu.udel.irl.atlas.analysis;

import edu.udel.irl.atlas.babelnet.SynsetOp;
import edu.udel.irl.atlas.parser.ParserOp;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import edu.udel.irl.atlas.util.UPOSMapper;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.opennlp.OpenNLPLemmatizerFilter;
import org.apache.lucene.analysis.opennlp.OpenNLPTokenizer;
import org.apache.lucene.analysis.opennlp.tools.NLPLemmatizerOp;
import org.apache.lucene.analysis.opennlp.tools.NLPSentenceDetectorOp;
import org.apache.lucene.analysis.opennlp.tools.NLPTokenizerOp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class AtlasAnalyzer extends StopwordAnalyzerBase {
    private static final AtlasConfiguration CONFIG = AtlasConfiguration.getInstance();
    private static final String MODELS_FOLDER = CONFIG.getModelFolder();
    private static final String MAPPER_FOLDER = CONFIG.getPOSMapperFolder();
    private NLPTokenizerOp tokenizerOp;
    private NLPSentenceDetectorOp sentenceDetectorOp;
    private NLPLemmatizerOp lemmatizerOp;
    private ParserOp parserOp;
    private SynsetOp synsetOp;

    public AtlasAnalyzer(){
        try {
            this.tokenizerOp = new NLPTokenizerOp(new TokenizerModel(
                    new File(MODELS_FOLDER, CONFIG.getTokenizerModel())));

            this.sentenceDetectorOp = new NLPSentenceDetectorOp(new SentenceModel(
                    new File(MODELS_FOLDER, CONFIG.getSentenceModel())));

            this.lemmatizerOp = new NLPLemmatizerOp(new FileInputStream(
                    new File(MODELS_FOLDER, CONFIG.getLemmatizerDict())), null);

            //initial ParserOp and SynsetOp classes by using reflection.
            Class parserClass = Class.forName("edu.udel.irl.atlas.parser." + CONFIG.getParserName());
//            Class parserClass = Class.forName(this.getClass().getPackage().getName() + "." + CONFIG.getParserName());
            Constructor parserConstructor = parserClass.getConstructor(File.class);
            this.parserOp = (ParserOp) parserConstructor.newInstance(
                    new File(MODELS_FOLDER, CONFIG.getParserModel()));

            Class synsetClass = Class.forName("edu.udel.irl.atlas.babelnet." + CONFIG.getSynsetDictName());
            Constructor synsetConstructor = synsetClass.getConstructor(UPOSMapper.class);
            this.synsetOp = (SynsetOp) synsetConstructor.newInstance(new UPOSMapper(
                    new File(MAPPER_FOLDER, CONFIG.getPOSMapperFile())));
        } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final OpenNLPTokenizer source;
        try {
            source = new OpenNLPTokenizer(attributeFactory(fieldName), this.sentenceDetectorOp, this.tokenizerOp);
            TokenStream target = new ParsePayloadFilter(source, this.parserOp);
            target = new OpenNLPLemmatizerFilter(target, this.lemmatizerOp);
            if(CONFIG.usingStopwords()) {
                if(CONFIG.getStopwordsFile().equalsIgnoreCase("default"))
                    target = new StopFilter(target, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
                else {
                    target = new StopFilter(target, loadStopwordSet(
                            new File(CONFIG.getStopwordsFolder(), CONFIG.getStopwordsFile()).toPath()));
                }
            }
            target = new SynsetFilter(target, this.synsetOp);
            return new TokenStreamComponents(source, target);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream target = new ParsePayloadFilter(in, this.parserOp);
        target = new OpenNLPLemmatizerFilter(target, this.lemmatizerOp);
        return new SynsetFilter(target, this.synsetOp);
    }
}
