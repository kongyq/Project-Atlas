package edu.udel.irl.atlas.synsim.nasari;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;

import java.util.Arrays;

/**
 * Created by mike on 6/24/18.
 */
public class WeightedOverlap {
    public WeightedOverlap(){}

//    //only use for wrap up byte[].
//    public static class ByteArray{
//
//        private byte[] array;
//
//        private ByteArray(byte[] array){
//            this.array = array;
//        }
//
//        public boolean equals(Object o) {
//            return o instanceof ByteArray && Arrays.equals(this.array, ((ByteArray) o).array);
//        }
//
//        public int hashCode(){
//            return this.array.length;
//        }
//    }

    public static <T> double compare(T[] v1,T[] v2){
        ObjectSet<T> overlaps = new ObjectOpenHashSet<>(v1);
        overlaps.retainAll(Arrays.asList(v2));
        return v1.length > v2.length?compareSmallerWithBigger(overlaps, v2, v1):compareSmallerWithBigger(overlaps, v1, v2);
    }

    public static double compare(int[] v1, int[] v2) {
        IntSet overlaps = new IntOpenHashSet(v1);
        IntSet temp = new IntOpenHashSet(v2);
        overlaps.retainAll(temp);
//        overlaps.retainAll(Arrays.asList(v2));
        return v1.length > v2.length?compareSmallerWithBigger(overlaps, v2, v1):compareSmallerWithBigger(overlaps, v1, v2);
    }

    //use inner class to wrap byte array as an object. But the performance is poor.
//    public static double compare(byte[][] v1, byte[][] v2){
//
//        ByteArray[] arrays_v1 = Arrays.stream(v1).map(ByteArray::new).toArray(ByteArray[]::new);
//        ByteArray[] arrays_v2 = Arrays.stream(v2).map(ByteArray::new).toArray(ByteArray[]::new);
//
//        return compare(arrays_v1,arrays_v2);
//    }
//
    public static double compare(byte[][] v1, byte[][] v2){

        ObjectList<byte[]> overlaps_v1 = new ObjectArrayList<>();
        ObjectList<byte[]> overlaps_v2 = new ObjectArrayList<>();
        for (byte[] aV1 : v1) {
            for (byte[] aV2 : v2) {
                if (Arrays.equals(aV1, aV2)) {
                    overlaps_v1.add(aV1);
                    overlaps_v2.add(aV2);
                    break;
                }
            }

        }

        return v1.length > v2.length?compareSmallerWithBigger(overlaps_v2, overlaps_v1, v2, v1)
                :compareSmallerWithBigger(overlaps_v1, overlaps_v2, v1, v2);
    }



    private static double compareSmallerWithBigger(IntSet overlaps, int[] v1, int[] v2) {
        double nominator = 0.0D;
        double normalization = 0.0D;
        if(overlaps.isEmpty()) {
            return 0.0D;
        } else {
            Int2IntMap indexToPosition1 = new Int2IntOpenHashMap(v1.length);
            Int2IntMap indexToPosition2 = new Int2IntOpenHashMap(v2.length);

            int i;
            for(i = 0; i < v1.length; ++i) {
                indexToPosition1.put(v1[i], i);
            }

            for(i = 0; i < v2.length; ++i) {
                indexToPosition2.put(v2[i], i);
            }

            i = 1;

            for(IntIterator iter = overlaps.iterator(); iter.hasNext(); ++i) {
                int overlap = iter.nextInt();
                nominator += 1.0D / (double)(indexToPosition1.get(overlap) + 1 + indexToPosition2.get(overlap) + 1);
                normalization += 1.0D / (double)(2 * i);
            }

            return nominator != 0.0D && normalization != 0.0D?nominator / normalization:0.0D;
        }
    }

    private static double compareSmallerWithBigger(ObjectList overlaps_v1, ObjectList overlaps_v2, byte[][] v1, byte[][] v2) {
        double nominator = 0.0D;
        double normalization = 0.0D;
        if(overlaps_v1.isEmpty()) {
            return 0.0D;
        } else {
            Object2IntMap indexToPosition1 = new Object2IntOpenHashMap(v1.length);
            Object2IntMap indexToPosition2 = new Object2IntOpenHashMap(v2.length);

            int i;
            for(i = 0; i < v1.length; ++i) {
                indexToPosition1.put(v1[i], i);
            }

            for(i = 0; i < v2.length; ++i) {
                indexToPosition2.put(v2[i], i);
            }

            i = 1;

            for(int idx = 0; idx < overlaps_v1.size(); ++idx, ++i){
                nominator += 1.0D / (double)(indexToPosition1.getInt(overlaps_v1.get(idx)) + 1 + indexToPosition2.getInt(overlaps_v2.get(idx)) + 1);
                normalization += 1.0D / (double)(2 * i);
            }

            return nominator != 0.0D && normalization != 0.0D?nominator / normalization:0.0D;
        }
    }

    private static <T> double compareSmallerWithBigger(ObjectSet<T> overlaps, T[] v1, T[] v2) {
        double nominator = 0.0D;
        double normalization = 0.0D;
        if(overlaps.isEmpty()) {
            return 0.0D;
        } else {
            Object2IntMap indexToPosition1 = new Object2IntOpenHashMap(v1.length);
            Object2IntMap indexToPosition2 = new Object2IntOpenHashMap(v2.length);

            int i;
            for(i = 0; i < v1.length; ++i) {
                indexToPosition1.put(v1[i], i);
            }

            for(i = 0; i < v2.length; ++i) {
                indexToPosition2.put(v2[i], i);
            }

            i = 1;

            for(ObjectIterator iter = overlaps.iterator(); iter.hasNext(); ++i) {
                T overlap = (T) iter.next();
                nominator += 1.0D / (double)(indexToPosition1.getInt(overlap) + 1 + indexToPosition2.getInt(overlap) + 1);
                normalization += 1.0D / (double)(2 * i);
            }

            return nominator != 0.0D && normalization != 0.0D?nominator / normalization:0.0D;
        }
    }
}
