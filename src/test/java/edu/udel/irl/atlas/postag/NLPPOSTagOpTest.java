package edu.udel.irl.atlas.postag;

import edu.udel.irl.atlas.util.AtlasConfiguration;
import opennlp.tools.postag.POSModel;
import org.apache.lucene.analysis.opennlp.tools.NLPPOSTaggerOp;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class NLPPOSTagOpTest {
    private final File model = new File(AtlasConfiguration.getInstance().getModelFolder(), AtlasConfiguration.getInstance().getPOSTaggerModel());
    NLPPOSTaggerOp taggerOp = new NLPPOSTaggerOp(new POSModel(model));

    public NLPPOSTagOpTest() throws IOException {

    }

    @Test
    public void test(){
//        String[] words = new String[]{"British", "Chunnel", "impact"};
        String[] words = new String[]{"The", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog", "."};
//        String[] words = new String[]{"illegal","technology", "transfer"};
        String[] poses = taggerOp.getPOSTags(words);
        for (String pos : poses) {
            System.out.println(pos);
        }
    }
}
