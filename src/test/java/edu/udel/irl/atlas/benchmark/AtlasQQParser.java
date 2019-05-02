package edu.udel.irl.atlas.benchmark;

import edu.udel.irl.atlas.search.AtlasQueryParser;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;

public class AtlasQQParser implements QualityQueryParser {

    private String[] qqNames;
    private String indexField;
    private IndexReader reader;
    ThreadLocal<AtlasQueryParser> atlasQueryParser = new ThreadLocal<>();

    public AtlasQQParser(String qqName, String indexField, IndexReader reader) {
        this(new String[]{qqName}, indexField, reader);
    }

    public AtlasQQParser(String[] qqNames, String indexField, IndexReader reader) {
        this.qqNames = qqNames;
        this.indexField = indexField;
        this.reader = reader;
    }

    @Override
    public Query parse(QualityQuery qq) {
        AtlasQueryParser aqp = atlasQueryParser.get();
        if (aqp == null) {
            try {
                aqp = new AtlasQueryParser(indexField, reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            atlasQueryParser.set(aqp);
        }

        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        for (String qqName : qqNames) {
            try {
                if(qqName.equals("title")) {
                    bq.add(aqp.parse(qq.getValue(qqName),true, false, true), BooleanClause.Occur.MUST);
                }else{
                    bq.add(aqp.parse(qq.getValue(qqName), false, false,true), BooleanClause.Occur.SHOULD);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bq.build();

    }
}
