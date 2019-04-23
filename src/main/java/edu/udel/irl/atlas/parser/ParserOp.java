package edu.udel.irl.atlas.parser;

import java.util.List;

/**
 * Refactored to make parsers thread-safe
 */
public abstract class ParserOp {

    public abstract OpParser createParser();

    /**
     * inner class contains components that need to be created for each thread
     * @param <P>
     */
    public abstract class OpParser<P>{
        public P parser;

        OpParser(P parser){
            this.parser = parser;
        }

        public abstract String[] getSentence();

        public abstract void parseSent(String[] sentence);

        public abstract List<byte[]> getCodeList();

        public abstract List<byte[]> getCodeList(short sentNum);

        public abstract String[] getPosTags();
    }
}
