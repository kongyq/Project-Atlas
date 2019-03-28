package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.synsim.ADWSynsetSimilarity;
import edu.udel.irl.atlas.synsim.SynsetSimilarity;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import it.unimi.dsi.fastutil.objects.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

/**
 * The most important method is {@link #parse(String)}.
 * The syntax for query strings is as follows:
 * A human readable sentence or a phrase composed by several words.
 */
public class AtlasQueryParser{
    private final double THRESHOLD = AtlasConfiguration.getInstance().getSimilarityThreshold();
    private final boolean crossAllPOS = AtlasConfiguration.getInstance().isExpansionCrossPOS();

    private final Analyzer analyzer;
    private final String field;

    private final List<String> synsetsInIndex;

    /**
     * A hashmap stores all similar terms of each term in the query and theirs corresponding
     * payloads and similarities of original query terms.
     * <P>
     *     {@code Map<SimilarTerm, Map<Payload, Similarity>>}
     * </P>
     */
    private Object2ObjectMap<Term, Object2DoubleOpenHashMap<BytesRef>> queryMap;

    /**
     * Create a {@code AtlasQuery} parser.
     * @param field the default field for query terms
     * @param analyzer used to find terms in the query text
     * @param reader used to find all similar terms of query terms in the index
     * @throws IOException if the index cannot be opened
     */
    public AtlasQueryParser(String field, Analyzer analyzer, IndexReader reader) throws IOException {
//        assert analyzer instanceof AtlasAnalyzer;
        this.field = Objects.requireNonNull(field);
        this.analyzer = Objects.requireNonNull(analyzer);

        // Add all synsets of the index into synsetList
        Terms terms = MultiFields.getTerms(Objects.requireNonNull(reader), field);
        this.synsetsInIndex = new ArrayList<>((int) terms.size());
        TermsEnum termsEnum = terms.iterator();
        while(termsEnum.next() != null){
            synsetsInIndex.add(termsEnum.term().utf8ToString());
        }
    }

    /**
     * Parse a query string, returning a {@link edu.udel.irl.atlas.search.AtlasQuery}
     * @param queryText the query string to be parsed.
     * @return {@code AtlasQuery}
     * @throws IOException if the query map fail to be created.
     */
    public SpanQuery parse(String queryText) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream(field, queryText);
        atlazing(tokenStream, crossAllPOS);

        return createAtlasQuery(createSpanOrQuery(field, queryMap.keySet()), queryMap);
    }

    /**
     * Create a SpanOrQuery from the similar terms set.
     * @param field field name
     * @param terms set of similar terms
     * @return a {@code SpanOrQuery}
     */
    public SpanOrQuery createSpanOrQuery(String field, Set<Term> terms){
        assert !terms.isEmpty();
        List<SpanTermQuery> spanTermQueries = new ArrayList<>(terms.size());
        for(Term term: terms){
            spanTermQueries.add(new SpanTermQuery(term));
        }
        return new SpanOrQuery(spanTermQueries.toArray(new SpanTermQuery[0]));
    }

    /***
     * Create a AtlasQuery from a span query and a query map
     * @param in {@code SpanQuery}
     * @param queryMap the query map for the span query
     * @return a {@code AtlasQuery}
     */
    public AtlasQuery createAtlasQuery(SpanQuery in, Map queryMap){
        return new AtlasQuery(in, queryMap);
    }

    /**
     * Core method for parsing query string. Use {@link TokenStream} of the query to create a query map.
     * @param queryStream {@link TokenStream} of the query string.
     * @param crossAllPOS whether find the similar terms over other type of POS.
     * @throws IOException if the {@link TokenStream} fail to read.
     */
    private void atlazing(TokenStream queryStream, boolean crossAllPOS) throws IOException {
        assert queryStream.hasAttribute(CharTermAttribute.class) && queryStream.hasAttribute(PayloadAttribute.class);

        CharTermAttribute termAtt = queryStream.getAttribute(CharTermAttribute.class);
        PayloadAttribute payloadAtt = queryStream.getAttribute(PayloadAttribute.class);

        queryMap = new Object2ObjectOpenHashMap<>();
        //TODO: need to modify this varible to use configuration file.
        SynsetSimilarity synsetSimilarity = ADWSynsetSimilarity.getInstance();

        // get query's payloads and terms
        List<BytesRef> queryPayloads = new ArrayList<>();
        List<String> queryTokens = new ArrayList<>();
        while(queryStream.incrementToken()){
            queryPayloads.add(payloadAtt.getPayload());
            queryTokens.add(termAtt.toString());
        }
        assert queryPayloads.size() == queryTokens.size();

        for(String synset: synsetsInIndex){
            Object2DoubleOpenHashMap<BytesRef> payloadSimMap = new Object2DoubleOpenHashMap<>();
            for(int i = 0; i < queryTokens.size(); i ++){
                String token = queryTokens.get(i);

                // if the expansion only for same type of POS, check synset and token POS.
                if(!crossAllPOS && token.charAt(token.length()-1) != synset.charAt(synset.length()-1)){
                    continue;
                }

                double similarity = synsetSimilarity.compare(token, synset);
                if(similarity >= THRESHOLD){
                    payloadSimMap.put(queryPayloads.get(i), similarity);
                }
            }
            if (!payloadSimMap.isEmpty()) {
                queryMap.put(new Term(field, synset), payloadSimMap);
            }
        }
        assert queryMap.size() >= queryTokens.size();
    }
}
