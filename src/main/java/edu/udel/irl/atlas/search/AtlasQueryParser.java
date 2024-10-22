package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.analysis.AtlasAnalyzer;
import edu.udel.irl.atlas.search.function.AtlasScoreFunction;
import edu.udel.irl.atlas.search.function.ScoreFunction;
import edu.udel.irl.atlas.synsim.SynsetSimilarity;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import edu.udel.irl.atlas.util.SynsetFormatChecker;
import it.unimi.dsi.fastutil.objects.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * The most important method is {@link #parse(String)}.
 * The syntax for query strings is as follows:
 * A human readable sentence or a phrase composed by several words.
 */
public class AtlasQueryParser{
    private final double THRESHOLD = AtlasConfiguration.getInstance().getSimilarityThreshold();
    private final boolean crossAllPOS = AtlasConfiguration.getInstance().isExpansionCrossPOS();
    private final boolean expandSynset = AtlasConfiguration.getInstance().expandSynset();
    private final String synsetComparator = AtlasConfiguration.getInstance().getSynsetComparatorClassName();

    private final Analyzer analyzer;
    private final String field;
    private final ScoreFunction function;

    private final List<String> synsetsInIndex;

    /**
     * A hashmap stores all similar terms of each term in the query and theirs corresponding
     * payloads and similarities of original query terms.
     * <P>
     *     {@code Map<SimilarTerm, Map<Payload, Similarity>>}
     * </P>
     */
    private Object2ObjectMap<Term, Object2FloatMap<BytesRef>> queryMap;

    /**
     * Create a {@code AtlasQuery} parser by using default {@code AtlasAnalyzer and AtlasScoreFunction}
     * @param field the field for query terms
     * @param reader the IndexReader
     * @throws IOException if the index cannot be opened
     */
    public AtlasQueryParser(String field, IndexReader reader) throws IOException {
        this(field, new AtlasAnalyzer(), reader, new AtlasScoreFunction());
    }

    /**
     * Create a {@code AtlasQuery} parser by using default {@code AtlasScoreFunction}.
     * @param field the field for query terms
     * @param analyzer the analyzer used for parsing query text
     * @param reader the IndexReader
     * @throws IOException if the index cannot be opened
     */
    public AtlasQueryParser(String field, Analyzer analyzer, IndexReader reader) throws IOException {
        this(field, analyzer, reader, new AtlasScoreFunction());
    }
    /**
     * Create a {@code AtlasQuery} parser.
     * @param field the default field for query terms
     * @param analyzer used to find terms in the query text
     * @param reader used to find all similar terms of query terms in the index
     * @throws IOException if the index cannot be opened
     */
    public AtlasQueryParser(String field, Analyzer analyzer, IndexReader reader, ScoreFunction function) throws IOException {
        this.field = Objects.requireNonNull(field);
        this.analyzer = Objects.requireNonNull(analyzer);
        this.function = function;
        // Add all synsets of the index into synsetList
        Terms terms = MultiFields.getTerms(Objects.requireNonNull(reader), field);
        this.synsetsInIndex = new ArrayList<>();
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
    public Query parse(String queryText) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream(field, queryText);
//        atlazing(analyzer.tokenStream(field, queryText), crossAllPOS);
        atlazing(tokenStream, crossAllPOS);
        tokenStream.close();

//        System.out.println(queryMap.keySet().size());
//        for (Term term : queryMap.keySet()) {
//            System.out.println(term.toString());
//        }

        if(queryMap.isEmpty()) return new MatchNoDocsQuery("No similar term of the query found in the index!");
        if(queryMap.size() == 1) return new AtlasQuery(createSpanOrQuery(field, queryMap.keySet()), function, queryMap);
        return new AtlasQuery(createSpanNearQuery(field, queryMap.keySet()), function, queryMap);
    }

    public Query parse(String queryText, boolean isMust, boolean includeSpanScore, boolean includePayloadScore) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream(field, queryText.replaceAll("[/-]", " "));
        atlazing(tokenStream, crossAllPOS);
        tokenStream.close();

        if(queryMap.isEmpty()) return new MatchNoDocsQuery("No similar term of the query found in the index!");
        if(queryMap.size() == 1) return new AtlasQuery(new SpanTermQuery(queryMap.keySet().iterator().next()), function, queryMap,includeSpanScore, includePayloadScore);
        if(isMust) {
            return new AtlasQuery(createSpanNearQuery(field, queryMap.keySet()), function, queryMap, includeSpanScore, includePayloadScore);
        }
        return new AtlasQuery(createSpanOrQuery(field, queryMap.keySet()), function, queryMap, includeSpanScore, includePayloadScore);
    }

    /**
     * Create a SpanOrQuery from the similar terms set.
     * @param field field name
     * @param terms set of similar terms
     * @return a {@code SpanOrQuery}
     */
    public SpanOrQuery createSpanOrQuery(String field, Set<Term> terms){
//        assert !terms.isEmpty() : "CreateSpanOrQuery: no Terms!";
        List<SpanTermQuery> spanTermQueries = new ArrayList<>(terms.size());
        for(Term term: terms){
            spanTermQueries.add(new SpanTermQuery(term));
        }
        return new SpanOrQuery(spanTermQueries.toArray(new SpanTermQuery[0]));
    }

    /**
     * Create a SpanNearQuery from the similar terms set.
     * @param field field name
     * @param terms set of similar terms
     * @return a {@code SpanNearQuery}
     */
    public SpanNearQuery createSpanNearQuery(String field, Set<Term> terms) {
        SpanNearQuery.Builder builder = new SpanNearQuery.Builder(field, false);
        for (Term term : terms) {
            builder.addClause(new SpanTermQuery(term));
        }
        builder.setSlop(Integer.MAX_VALUE);
        return builder.build();
    }

    /**
     * Core method for parsing query string. Use {@link TokenStream} of the query to create a query map.
     * @param queryStream {@link TokenStream} of the query string.
     * @param crossAllPOS whether find the similar terms over other type of POS.
     * @throws IOException if the {@link TokenStream} fail to read.
     */
    private void atlazing(TokenStream queryStream, boolean crossAllPOS) throws IOException {
        assert queryStream.hasAttribute(CharTermAttribute.class) && queryStream.hasAttribute(PayloadAttribute.class)
                :"atlazing failure: char or payload Attribute not found!";

        CachingTokenFilter stream = new CachingTokenFilter(queryStream);

        CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
        PayloadAttribute payloadAtt = stream.getAttribute(PayloadAttribute.class);

        queryMap = new Object2ObjectOpenHashMap<>();
        SynsetSimilarity synsetSimilarity = getSynsetComparator();
        assert synsetSimilarity != null;

        // get query's payloads and terms
        List<BytesRef> queryPayloads = new ArrayList<>();
        List<String> queryTokens = new ArrayList<>();

        stream.reset();
        while(stream.incrementToken()){
            queryPayloads.add(payloadAtt.getPayload());
            queryTokens.add(termAtt.toString());
        }
        assert queryPayloads.size() == queryTokens.size():"atlazing failure: payload size does not match token size!";

//        System.out.println(synsetsInIndex.size());

        Object2FloatOpenHashMap<BytesRef> payloadSimMap = new Object2FloatOpenHashMap<>();


//        int c = 0;
        for(String synset: synsetsInIndex){
//            if(c % 10000 == 0){
//                System.out.println(c);
//            }
//            c++;
//            System.out.println(synset + " -> ");
            for(int i = 0; i < queryTokens.size(); i ++){
                String token = queryTokens.get(i);

                if(token.equals(synset)) {
                    payloadSimMap.put(queryPayloads.get(i), 1f);
                }else if(expandSynset && SynsetFormatChecker.check(synset) && SynsetFormatChecker.check(token)){
                    // if the expansion only for same type of POS, check synset and token POS.
                    if (crossAllPOS || (token.charAt(token.length() - 1) == synset.charAt(synset.length() - 1))) {
                        float similarity = (float) synsetSimilarity.compare(token, synset);
//                    System.out.print(token + " " + similarity);
                        if (similarity >= THRESHOLD) {
//                        System.out.print(":" + queryPayloads.get(i));
                            payloadSimMap.put(queryPayloads.get(i), similarity);
                        }
                    }
//                System.out.println();
                }
            }
            if (!payloadSimMap.isEmpty()) {
                queryMap.put(new Term(field, synset), new Object2FloatOpenHashMap<>(payloadSimMap));
                payloadSimMap.clear();
            }
        }

//        System.out.println(queryMap.size());
//        System.out.println(queryTokens.size());

//        assert queryMap.size() >= queryTokens.size()
//                : "atlazing failure: queryMap size should be larger or equal to query tokens";
    }

    /**
     * Use reflection to get synset comparator based on the configuration file
     * @return a SynsetSimilarity instance
     */
    private SynsetSimilarity getSynsetComparator(){
        try {
            Class syncompClass = Class.forName("edu.udel.irl.atlas.synsim." + synsetComparator);
            return (SynsetSimilarity) syncompClass.getDeclaredMethod("getInstance").invoke(null);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
