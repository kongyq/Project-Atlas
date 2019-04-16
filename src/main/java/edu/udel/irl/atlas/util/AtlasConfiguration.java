package edu.udel.irl.atlas.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.util.logging.Logger;

public class AtlasConfiguration {
    private PropertiesConfiguration config = null;
    private static final Logger LOGGER = Logger.getLogger(AtlasConfiguration.class.getName());
    private static AtlasConfiguration instance = null;
    private static String CONFIG_DIR = "config/";
    public  static String CONFIG_FILE = "atlas.properties";

    private AtlasConfiguration(){
        File configFile = new File(CONFIG_DIR, CONFIG_FILE);
        boolean bDone = false;
        if(configFile.exists()){
            LOGGER.info("Loading " + CONFIG_FILE + " FROM " + configFile.getAbsolutePath());

            try {
                this.config = new PropertiesConfiguration(configFile);
                bDone = true;
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }

        if(!bDone){
            LOGGER.info(CONFIG_FILE + " is missing. Please check that the file is available in the config folder.");
            LOGGER.info("Atlas starts with empty configuration");
            this.config = new PropertiesConfiguration();
        }
    }

    public void setConfigurationFile(File configurationFile) {
        LOGGER.info("Changing configuration properties to " + configurationFile);

        try {
            this.config = new PropertiesConfiguration(configurationFile);
            this.config.setBasePath(configurationFile.getParentFile().getAbsolutePath());
        } catch (ConfigurationException e) {
            e.printStackTrace();
            LOGGER.info("Setting Atlas to an empty configuration");
            this.config = new PropertiesConfiguration();
        }

    }

    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
        if(c != null && string != null) {
            try {
                return Enum.valueOf(c, string.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static synchronized AtlasConfiguration getInstance(){
        if(instance == null){
            instance = new AtlasConfiguration();
        }
        return instance;
    }

    //----------Index------------------------

    public String getResourceFolder(){return this.config.getString("atlas.resourceDir");}

    public String getModelFolder(){return this.config.getString("model.path");}

    public String getTokenizerModel(){return this.config.getString("tokenizer.model");}

    public String getSentenceModel(){return this.config.getString("sentenceDetector.model");}

    public String getParserName(){return this.config.getString("parser.name") + "ParserOp";}

    public String getParserModel(){return this.config.getString(this.config.getString("parser.name") + ".parser.model");}

    public String getLemmatizerDict(){return this.config.getString("lemmatizer.dict");}

    public String getSynsetDictName(){return this.config.getString("synsetDict.name") + "SynsetOp";}

    public String getWordNetFolder(){return this.config.getString("wordnet.dict.path");}

    public String getPOSMapperFile(){return this.config.getString(this.config.getString("synsetDict.name") + ".pos.mapper.file");}

    public String getPOSMapperFolder(){return this.config.getString("pos.mapper.path");}

    public boolean usingStopwords(){return this.config.getBoolean("stopwords.use");}

    public String getStopwordsFile(){return this.config.getString("stopwords.file");}

    public String getStopwordsFolder(){return this.config.getString("stopwords.path");}

    public boolean skipPunctuation(){return this.config.getBoolean("misc.punctuation.skip");}

    //----------Search-----------------------

    public double getSimilarityThreshold(){return this.config.getDouble(getSynsetComparatorName() + ".synset.similarity.threshold");}

    public boolean isExpansionCrossPOS(){return this.config.getBoolean("synset.expansion.crossPOS");}

    private String getSynsetComparatorName(){return this.config.getString("synset.comparator.name");}

    public String getSynsetComparatorClassName(){return getSynsetComparatorName() + "SynsetSimilarity";}

    public String getNasariVectorType(){return this.config.getString("nasari.vector.type");}

    public String getNasariVectorFile(){return this.config.getString(getNasariVectorType() + ".nasari.vectorFile");}

    public boolean isLexicalModelCompressed(){return this.config.getBoolean("lexical.model.compress");}

    //----------Advance-----------------------

    public String getIndexDocIdField(){return this.config.getString("documentID.field");}

    public String getIndexBodyField(){return this.config.getString("context.field");}

    public boolean sortBarQueryPriority(){return this.config.getBoolean("queryArc.first");}

    public float getHarmonicMeanExponent(){
        return (this.config.getString("harmonicMean.exponent").equalsIgnoreCase("e")) ?
                (float) Math.E : this.config.getFloat("harmonicMean.exponent");
    }

    public String getDocIdField(){return this.config.getString("docId.field");}
}
