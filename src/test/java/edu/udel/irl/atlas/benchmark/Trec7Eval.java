package edu.udel.irl.atlas.benchmark;

import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.*;
import java.nio.file.Path;

public class Trec7Eval {

    private final File topicFile = new File("/home/mike/Documents/runs/topics.trec7.351-400");
    private final File qrelFile = new File("/home/mike/Documents/runs/qrels.trec7.351-400");
//    private final Path indexPath = new File("/home/mike/Documents/Index/Trec7").toPath();
    private final Path indexPath = new File("/home/mike/Documents/Index/Trec7Legacy").toPath();
    private final Directory directory = NIOFSDirectory.open(indexPath);
    private final IndexReader reader = DirectoryReader.open(directory);
    private final IndexSearcher searcher = new IndexSearcher(reader);

    private final String docNameField = "docId";
    private final String docBodyField = "text";
    private final String docTitleField = "title";

    public Trec7Eval() throws IOException {

    }

    public void run() throws Exception {
        PrintWriter LOGGER = new PrintWriter(System.out, true);

        TrecTopicsReader qReader = new TrecTopicsReader();
        QualityQuery[] qqs = qReader.readQueries(new BufferedReader(new FileReader(topicFile)));

        Judge judge = new TrecJudge(new BufferedReader(new FileReader(qrelFile)));

        judge.validateData(qqs, LOGGER);

//        QualityQueryParser qqParser = new AtlasQQParser(new String[]{"title","description"}, docBodyField, reader);
//        QualityQueryParser qqParser = new AtlasQQParser("title", docBodyField, reader);
//        QualityQueryParser qqParser = new SimpleQQParser("title", docBodyField);
        QualityQueryParser qqParser = new SimpleQQParser(new String[]{"title", "description", "narrative"}, docBodyField);

        QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, docNameField);

        SubmissionReport submitLog = null;
        QualityStats stats[] = qrun.execute(judge, submitLog, LOGGER);

        QualityStats avg = QualityStats.average(stats);
        avg.log("SUMMARY", 2, LOGGER, "");

        this.directory.close();
    }

    public static void main(String[] args) throws Exception {

        Trec7Eval eval = new Trec7Eval();
        eval.run();
//        SimpleQQParser simpleQQParser = new SimpleQQParser(null,null);
    }

}
