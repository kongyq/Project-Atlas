package edu.udel.irl.atlas.parser;

import java.util.List;

public interface ParserOp<T> {
//    T parseSent(String[] sentence);

    void parseSent(String[] sentence);

    String[] getPosTags();

    T getParse();

    List<byte[]> getCodeList();

    List<byte[]> getCodeList(Short sentNum);
}
