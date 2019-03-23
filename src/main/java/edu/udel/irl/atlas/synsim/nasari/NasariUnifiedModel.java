package edu.udel.irl.atlas.synsim.nasari;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

/**
 * Created by mike on 6/19/18.
 */
public class NasariUnifiedModel implements NasariModel<int[]>{
    private static final long ONE_GB = 1024 * 1024 * 1024;  // = 2^30

    private static NasariUnifiedModel instance = null;

    private final Int2IntMap synset2vectorOffset;
    private final IntBuffer vectors;

    private NasariUnifiedModel(Int2IntMap synset2vectorOffset, IntBuffer vectors){
        this.synset2vectorOffset = Int2IntMaps.unmodifiable(synset2vectorOffset);
        this.vectors = vectors;
    }

    private static NasariUnifiedModel loadVectorFile(File vectorFile) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

//        int count = 1;
        //get number of lines(synset) and tabs
        System.out.println("Analyzing the vector file...");
        BufferedReader reader = Files.newBufferedReader(vectorFile.toPath());
        String line;
        int tabCount = 0;
        int lineCount = 0;
        while((line = reader.readLine()) != null){
            tabCount += countTabs(line);
            ++lineCount ;
        }
        reader.close();

        //initial synset2vector offset map.
        Int2IntMap synset2vectorOffset = new Int2IntOpenHashMap(lineCount);

        //memory mapping
        System.out.println("Reading #1 gigabyte of the vector file.");
        FileChannel fileChannel = new FileInputStream(vectorFile).getChannel();
        MappedByteBuffer buffer = fileChannel.map(
                        FileChannel.MapMode.READ_ONLY,
                        0,
                        Math.min(fileChannel.size(), Integer.MAX_VALUE));

        //store mapping times
        int bufferCount = 1;

        //allocate memory for vector space.
        IntBuffer vectors = ByteBuffer.allocateDirect(tabCount * 4).asIntBuffer();

        StringBuilder sb = new StringBuilder();
        while(buffer.hasRemaining()){

            sb.setLength(0);
            char c = (char) buffer.get();

            //read a line
            while(c != '\n'){
                sb.append(c);
                c = (char)buffer.get();
            }

            line = sb.toString();
            String[] lineSplited = line.split("\t");

            //get synset
            int synset = Integer.parseInt(lineSplited[0].substring(3,11));

            //add synset position map
            synset2vectorOffset.put(synset, vectors.position());

            //put vector length into buffer
            vectors.put(lineSplited.length - 2);

            for(int i = 2; i < lineSplited.length; i++){
                vectors.put(Integer.parseInt(lineSplited[i].substring(3,11)));
            }

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
//            if(count % 10000 == 0){
//                System.out.println(count);
//            }
//            count += 1;

        }
        stopwatch.stop();
        System.out.println("Vector file successfully loaded! cost " + stopwatch);

        return new NasariUnifiedModel(synset2vectorOffset, vectors);
    }

    private static int countTabs(String str) {
        int count = 0;
        for(int idx = 0; (idx = str.indexOf("\t", idx)) != -1; ++idx) {
            ++count;
        }
        return count;
    }

    //use soft copy to make sure the method is thread safe.
    public int[] getVectors(int synset){
        if(!this.synset2vectorOffset.containsKey(synset)){
            return new int[0];
        }
        //soft copy
        IntBuffer threadSafeShadowVectors = this.vectors.asReadOnlyBuffer();
        threadSafeShadowVectors.position(this.synset2vectorOffset.get(synset));
        int vectorLength = threadSafeShadowVectors.get();
        int[] vectors = new int[vectorLength];
        threadSafeShadowVectors.get(vectors);

        return vectors;
    }

    public static synchronized NasariUnifiedModel getInstance(File vectorFile){
        if (instance == null){
            try {
                instance = NasariUnifiedModel.loadVectorFile(vectorFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
}
