package org.threadly.concurrent.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.skife.jdbi.v2.DBI;
import org.threadly.concurrent.benchmark.BenchmarkDbi.RunRecord;
import org.threadly.util.StringUtils;

public class DatabaseGraphJsonExport {
  public static void main(String[] args) throws Exception {
    if (args.length != 5) {
      printUsage();
      System.exit(-1);
    }
    Class.forName("org.postgresql.Driver");
    DBI dbi = new DBI("jdbc:postgresql://" + args[0] + '/' + args[1],
                      args[2], args[3]);
    File outputFolder = new File(args[4]);
    if (! outputFolder.exists() && ! outputFolder.mkdirs()) {
      System.err.println("Could not create directory: " + args[4]);
      printUsage();
      System.exit(-2);
    } else if (outputFolder.exists() && ! outputFolder.isDirectory()) {
      System.err.println("Output directory is not a directory: " + args[4]);
      printUsage();
      System.exit(-3);
    }
    
    BenchmarkDbi benchmarkDbi = dbi.open(BenchmarkDbi.class);
    try {
      int maxClassGroupId = benchmarkDbi.getMaxClassGroupId();
      for (int cg = 0; cg < maxClassGroupId; cg++) {
        int minBenchmarkGroupId = benchmarkDbi.getMinBenchmarkGroupId(cg);
        int maxBenchmarkGroupId = benchmarkDbi.getMinBenchmarkGroupId(cg);
        List<List<RunRecord>> groupResults = new ArrayList<>((maxBenchmarkGroupId + 1) - minBenchmarkGroupId);
        int minMaxGroupRunId = Integer.MAX_VALUE;
        for (int bg = minBenchmarkGroupId; bg <= maxBenchmarkGroupId; bg++) {
          List<RunRecord> results = benchmarkDbi.getResultsForBenchmarkGroupId(bg, cg);
          int maxGroupRunId = results.get(results.size() - 1).getBenchmark_group_run_id();
          if (maxGroupRunId < minMaxGroupRunId) {
            minMaxGroupRunId = maxGroupRunId;
          }
          groupResults.add(results);
        }
        
        String classGroupIdentifier = benchmarkDbi.getClassGroupIdentifier(cg);
        if (StringUtils.isNullOrEmpty(classGroupIdentifier)) {
          classGroupIdentifier = "cg-" + cg;
        }
        
        JSONObject jsonRoot = new JSONObject();
        // TODO - generate json
        
        File outputFile = new File(outputFolder, classGroupIdentifier + ".json");
        try (Writer writer = new FileWriter(outputFile)) {
          jsonRoot.write(writer);
        }
      }
    } finally {
      benchmarkDbi.close();
    }
  }
  
  private static void printUsage() {
    System.err.println("java -cp <classpath> " + BenchmarkCollectionRunner.class + 
                         " <dbHost> <dbUser> <dbPass> <dbName> <outputDirectory>");
  }
}
