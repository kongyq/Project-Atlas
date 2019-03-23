package edu.udel.irl.atlas.synsim.nasari;

import com.google.common.base.Stopwatch;
import edu.udel.irl.atlas.util.AtlasConfiguration;
import edu.udel.irl.atlas.util.CompressionUtil;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;

/**
 * Created by mike on 6/26/18.
 */
public class NasariLexicalModel implements NasariModel<String[]>{

    private static final boolean isCompressionEnabled = AtlasConfiguration.getInstance().isLexicalModelCompressed();
    private static final long ONE_GB = 1024 * 1024 * 1024;  // = 2^30
    private static final int ONE_BLOCK = 1024 * 1024 * 1024;

    private static NasariLexicalModel instance = null;

    private final Int2LongMap synset2vectorOffset;
    private final ObjectList<ByteBuffer> vectorBlocks;

    private NasariLexicalModel(Int2LongMap synset2vectorOffset, ObjectList<ByteBuffer> vectors){
        this.synset2vectorOffset = Int2LongMaps.unmodifiable(synset2vectorOffset);
        this.vectorBlocks = ObjectLists.unmodifiable(vectors);
    }

    private static NasariLexicalModel loadVectorFile(File vectorFile) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        int count = 1;

        //get number of lines(synset) and tabs
        Stopwatch stopwatch2 = Stopwatch.createStarted();

        System.out.println("Analyzing the vector file...");
        LineNumberReader reader = new LineNumberReader(new FileReader(vectorFile));
        reader.skip(Integer.MAX_VALUE);
        int lineCount = reader.getLineNumber();
        reader.close();

        System.out.println(stopwatch2.stop());
//
        //initial synset2vector offset map.
        Int2LongMap synset2vectorOffset = new Int2LongOpenHashMap(lineCount);

        //memory mapping
        System.out.println("Reading #1 gigabyte of the vector file.");
        FileChannel fileChannel = new FileInputStream(vectorFile).getChannel();
        MappedByteBuffer buffer =
                fileChannel.map(
                        FileChannel.MapMode.READ_ONLY,
                        0,
                        Math.min(fileChannel.size(), Integer.MAX_VALUE));

        //store mapping times
        int bufferCount = 1;
        int blockNumber = 0;
        long bufferIndex = 0;

        //allocate memory for vector space.
        ObjectList<ByteBuffer> vectorBlocks = new ObjectArrayList<>();
        ByteBuffer currentVectorBlock = ByteBuffer.allocateDirect(ONE_BLOCK);

        ByteList bl = new ByteArrayList();

        while(buffer.hasRemaining()){

            bl.clear();
            byte b = buffer.get();

            while(b != '\t'){
                bl.add(b);
                b = buffer.get();
            }

            int synset = Integer.parseInt(new String(bl.toByteArray()).substring(3,11));

            //prepare to read vectors
            b = buffer.get();
            bl.clear();
            while(b != '\t'){b = buffer.get();}
            b = buffer.get();

            //read vectors
            while(b != '\n'){
                while(b != '_'){
                    bl.add(b);
                    b = buffer.get();
                }
                //skip vector values
                while(b != '\t' && b != '\n'){
                    b = buffer.get();
                }
            }

            byte[] vectors = bl.toByteArray();
            short vectorLength = (short) bl.size();

            long synsetVectorStartPosition = bufferIndex + (blockNumber * ONE_BLOCK);

            //check current block available space, if not enough to store the new vector, create a new buffer,
            // and add current buffer to the block list. then adjust synset to vector pointer.
            if(!isEnoughSpace(currentVectorBlock.position(),vectorLength)){
                System.out.println("created a new 1g bytebuffer.");
                vectorBlocks.add(currentVectorBlock);
                currentVectorBlock = ByteBuffer.allocateDirect(ONE_BLOCK);
                blockNumber ++;
                synsetVectorStartPosition = blockNumber * ONE_BLOCK;
            }

            //enable byte array compression
            if(isCompressionEnabled){
                byte[] compressedVectors = CompressionUtil.compress(vectors);
                currentVectorBlock.putShort((short) compressedVectors.length);
                currentVectorBlock.put(compressedVectors);
            }else {
                currentVectorBlock.putShort(vectorLength);
                currentVectorBlock.put(vectors);
            }

            synset2vectorOffset.put(synset, synsetVectorStartPosition);

            bufferIndex = currentVectorBlock.position();

            //remap if file larger than one gigabyte.
            if(buffer.position() > ONE_GB){
                final int newPosition = (int) (buffer.position() - ONE_GB);
                final long size = Math.min(fileChannel.size() - ONE_GB * bufferCount, Integer.MAX_VALUE);

                System.out.format("Reading #%d gigabyte of the vector file. Start: %d, size: %d%n",
                        bufferCount + 1,
                        ONE_GB * bufferCount,
                        size);

                buffer = fileChannel.map(
                        FileChannel.MapMode.READ_ONLY,
                        ONE_GB * bufferCount,
                        size);

                buffer.position(newPosition);
                bufferCount += 1;
            }
            if(count % 10000 == 0){
                System.out.println(count);
            }
            count += 1;

        }

        stopwatch.stop();
        System.out.println("Vector file successfully loaded! cost " + stopwatch);

        return new NasariLexicalModel(synset2vectorOffset, vectorBlocks);
    }

    public static synchronized NasariLexicalModel getInstance(File vectorFile){
        if (instance == null){
            try {
                instance = NasariLexicalModel.loadVectorFile(vectorFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String[] getVectors(int synset){
        if(!this.synset2vectorOffset.containsKey(synset)){
            return null;
        }
        long position = this.synset2vectorOffset.get(synset);
        int blockIndex = (int) (position / ONE_BLOCK);
        int blockOffset = (int) (position % ONE_BLOCK);

        //soft copy
        ByteBuffer threadSafeShadowVectors = this.vectorBlocks.get(blockIndex).asReadOnlyBuffer();
        threadSafeShadowVectors.position(blockOffset);
        short vectorLength = threadSafeShadowVectors.getShort();
        byte[] vectors = new byte[vectorLength];
        threadSafeShadowVectors.get(vectors);

        if(isCompressionEnabled){
            try {
                vectors = CompressionUtil.decompress(vectors);
            } catch (IOException | DataFormatException e) {
                e.printStackTrace();
            }
        }
        return new String(vectors).split("\t");
    }

    private static boolean isEnoughSpace(int position, int vectorLength){
        if(position <= ONE_BLOCK * 0.75){return true;}
        int available = ONE_BLOCK - 1 - position;

        return available >= vectorLength + 2;
    }
}
