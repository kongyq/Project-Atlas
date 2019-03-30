package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.search.function.AtlasScoreFunction;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AtlasWeight extends SpanWeight {
//    private final SpanQuery innerQuery;
    private final SpanWeight innerWeight;

    public AtlasWeight(SpanQuery innerQuery, IndexSearcher searcher, SpanWeight innerWeight, float boost) throws IOException {
        super(innerQuery, searcher, null, boost);
//        this.innerQuery = innerQuery;
        this.innerWeight = innerWeight;
    }

    @Override
    public void extractTerms(Set<Term> terms) {innerWeight.extractTerms(terms);}

    @Override
    public void extractTermContexts(Map<Term, TermContext> contexts) {
        innerWeight.extractTermContexts(contexts);
    }

    @Override
    public Spans getSpans(LeafReaderContext ctx, Postings requiredPostings) throws IOException {
        return innerWeight.getSpans(ctx, requiredPostings.atLeast(Postings.PAYLOADS));
    }

    @Override
    public boolean isCacheable(LeafReaderContext ctx) {
        return innerWeight.isCacheable(ctx);
    }

    @Override
    public Explanation explain(LeafReaderContext context, int doc) throws IOException {
        AtlasScorer scorer = (AtlasScorer)scorer(context);
        if (scorer == null || scorer.iterator().advance(doc) != doc)
            return Explanation.noMatch("No match");

        scorer.score();  // force freq calculation
        Explanation payloadExpl = scorer.getPayloadExplanation();

        return scorer.getPayloadExplanation();
    }

    @Override
    public SpanScorer scorer(LeafReaderContext context) throws IOException {
        Spans spans = getSpans(context, Postings.PAYLOADS);
        if (spans == null)
            return null;
        Similarity.SimScorer docScorer = innerWeight.getSimScorer(context);
        PayloadSpans payloadSpans = new PayloadSpans(spans);
        return new AtlasScorer(this, payloadSpans, docScorer, new AtlasScoreFunction(), null, field);
    }

    public class PayloadSpans extends FilterSpans implements SpanCollector {

        // store term and payload of each matching.
        public List<BytesRef> payloadList;
        public List<Term> termList;

        private PayloadSpans(Spans in) {
            super(in);
        }

        @Override
        protected AcceptStatus accept(Spans candidate) throws IOException {
            return AcceptStatus.YES;
        }

        @Override
        protected void doStartCurrentDoc() {
            payloadList.clear();
            termList.clear();
        }

        @Override
        public void collectLeaf(PostingsEnum postings, int position, Term term) throws IOException {
            termList.add(term);
            payloadList.add(postings.getPayload());
        }

        @Override
        public void reset() {}

        @Override
        protected void doCurrentSpans() throws IOException {
            in.collect(this);
        }
    }
}
