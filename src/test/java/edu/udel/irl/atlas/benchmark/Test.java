package edu.udel.irl.atlas.benchmark;

import org.apache.lucene.benchmark.byTask.feeds.DocData;
import org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException, NoMoreDataException {
        Trec7Test test = new Trec7Test();
        DocData docData = new DocData();
        while (docData.getName() == null || !docData.getName().equals("FR940628-2-00109")) {
            try {
                docData = test.getNextDoc(docData);
            }catch (NoMoreDataException e){
                System.out.println("Not Found!");
                System.exit(0);
                break;
            }
        }
//        System.out.println(docData.getName());
        test.showText(docData);
    }
}
