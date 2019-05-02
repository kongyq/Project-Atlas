package edu.udel.irl.atlas.chunker;

import edu.udel.irl.atlas.util.AtlasConfiguration;
import opennlp.tools.chunker.ChunkerModel;
import org.apache.lucene.analysis.opennlp.tools.NLPChunkerOp;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class NLPChunkerOPTest {

    private final File model = new File(AtlasConfiguration.getInstance().getModelFolder(), AtlasConfiguration.getInstance().getChunkerModel());
    NLPChunkerOp chunkerOp = new NLPChunkerOp(new ChunkerModel(model));

    public NLPChunkerOPTest() throws IOException {

    }

    @Test
    public void test() {
//        String[] words = new String[]{"British", "Chunnel", "impact"};
//        String[] poses = new String[]{"JJ", "NNP", "NN"};

        String[] words = new String[]{"The", "quick", "brown","fox","jumps","over","the","lazy","dog","."};
        String[] poses = new String[]{"DT","JJ","JJ","NN","VBZ","PP","DT","JJ","NN","."};
        String[] chunks = chunkerOp.getChunks(words, poses, null);
        for (String chunk : chunks) {
            System.out.println(chunk);
        }
    }

}
