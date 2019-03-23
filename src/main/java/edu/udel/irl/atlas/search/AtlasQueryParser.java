package edu.udel.irl.atlas.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AtlasQueryParser extends QueryBuilder {
    private TokenStream tokenStream;
    private final String field;
//    private final IndexReader reader;
    private final List<String> synsetInIndex;
    private int tokens;

    private Map<String, double[]> synsetPairSimilarity;
    private Map<String[], Double> relatedSynsetSimilarities;
    /**
     * Creates a new QueryBuilder using the given analyzer.
     *
     * @param analyzer
     */
    public AtlasQueryParser(String field, Analyzer analyzer, IndexReader reader) throws IOException {
        super(analyzer);
        this.field = field;
//        this.reader = reader;

        // Add all synsets of the index into synsetList
        Terms terms = MultiFields.getTerms(reader, field);
        this.synsetInIndex = new ArrayList<>((int) terms.size());
        TermsEnum termsEnum = terms.iterator();
        while(termsEnum.next() != null){
            synsetInIndex.add(termsEnum.term().utf8ToString());
        }
    }

    public Query parse(String queryText){
        if("*".equals(queryText.trim())){
            return new MatchAllDocsQuery();
        }

        this.tokenStream = analyzer.tokenStream(field, queryText);

        return null;
    }

    /**
     *
     * @param queryStream
     * @return
     */
    private TokenStream atlazing(TokenStream queryStream){
        queryStream.addAttribute(CharTermAttribute.class);
        queryStream.addAttribute(PositionIncrementAttribute.class);
        queryStream.addAttribute(TypeAttribute.class);

        return null;
    }
}
