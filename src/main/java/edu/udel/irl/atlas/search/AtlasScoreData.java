package edu.udel.irl.atlas.search;

import edu.udel.irl.atlas.util.Short2Bytes;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

import java.util.*;

@Deprecated
public class AtlasScoreData {

    private Map<Short, List<Term>> termMap;
    private Map<Short, List<byte[]>> payloadMap;

    public AtlasScoreData(){
        this.termMap = new HashMap<>();
        this.payloadMap = new HashMap<>();
    }

    public void add(Term term, BytesRef payload){
        byte[] codes = Arrays.copyOfRange(payload.bytes, payload.offset, payload.offset + payload.length);
        add(Short2Bytes.decodeShort(codes), term, codes);
    }

    public void add(short key, Term term, byte[] codes){
        this.termMap.putIfAbsent(key, new ArrayList<>());
        this.termMap.get(key).add(term);
        this.payloadMap.putIfAbsent(key, new ArrayList<>());
        this.payloadMap.get(key).add(codes);
    }

    public List<Term> getTermList(short key){return this.termMap.get(key);}

    public List<byte[]> getCodeList(short key){return this.payloadMap.get(key);}

    public Set<Short> getKeySet(){return this.termMap.keySet();}

    public void clear(){
        this.termMap.clear();
        this.payloadMap.clear();
    }
}
