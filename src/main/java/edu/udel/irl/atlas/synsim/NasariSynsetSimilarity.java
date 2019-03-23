package edu.udel.irl.atlas.synsim;

import edu.udel.irl.atlas.synsim.nasari.NasariLexicalModel;
import edu.udel.irl.atlas.synsim.nasari.NasariModel;
import edu.udel.irl.atlas.synsim.nasari.NasariUnifiedModel;
import edu.udel.irl.atlas.synsim.nasari.WeightedOverlap;
import edu.udel.irl.atlas.util.AtlasConfiguration;

import java.io.File;

public class NasariSynsetSimilarity extends SynsetSimilarity{
    private static final String vectorType = AtlasConfiguration.getInstance().getNasariVectorType();
    private static final File vectorFile = new File(AtlasConfiguration.getInstance().getNasariVectorFile());
    public static NasariSynsetSimilarity instance = null;
    private static NasariModel model;

    private NasariSynsetSimilarity(){
        if(vectorType.equals("unified")){
            model = NasariUnifiedModel.getInstance(vectorFile);
        }else if(vectorType.equals("lexical")){
            model = NasariLexicalModel.getInstance(vectorFile);
        }else{
            System.out.println("Wrong vector file type! Please check configuration in irons.properties.");
            System.exit(1);
        }
    }

    @Override
    public double compare(String synset1, String synset2){
        if(vectorType.equals("unified")) {
            return WeightedOverlap.compare(
                    (int[]) model.getVectors(convertSynsetIDToInt(synset1)),
                    (int[]) model.getVectors(convertSynsetIDToInt(synset2)));
        }else if(vectorType.equals("lexical")){
            return WeightedOverlap.compare(
                    (String[]) model.getVectors(convertSynsetIDToInt(synset1)),
                    (String[]) model.getVectors(convertSynsetIDToInt(synset2)));
        }
        System.out.println("");
        return 0D;
    }

    private int convertSynsetIDToInt(String synsetID){
        return Integer.parseInt(synsetID.substring(3,11));
    }

    public static synchronized NasariSynsetSimilarity getInstance(){
        if(instance == null){
            instance = new NasariSynsetSimilarity();
        }
        return instance;
    }
}
