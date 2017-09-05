package org.threadly.concurrent.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.threadly.concurrent.PriorityScheduledExecutor;
import org.threadly.concurrent.benchmark.dao.BenchmarkDao;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.util.ExceptionUtils;

public class BenchmarkCollectionRunner {
  protected static final int RUN_COUNT = 5;
  protected static final boolean INCLUDE_JAVA_BASELINE = false;
  protected static final boolean EXIT_ON_BENCHMARK_FAILURE = true;
  protected static final boolean DISCARD_FIRST_RUN = false;
  protected static final String SHELL = "bash";
  protected static final String JAVA_EXECUTE_CMD = "/usr/bin/java -Xmx4g -Xms2048m -Xss256k ";
  protected static final PriorityScheduledExecutor SCHEDULER;
  protected static final List<BenchmarkCase> BENCHMARKS_TO_RUN;
  
  static {
    SCHEDULER = new PriorityScheduledExecutor(4, 16, 10_000);
    SCHEDULER.prestartAllCoreThreads();
    
    ArrayList<BenchmarkCase> toRun = new ArrayList<>();
    
    String[][] executeScheduleRecurringThreadCases = new String[][]{{"2 4", "2 8", "2 10", "2 16", 
                                                                     "2 20", "2 32", "2 64", 
                                                                     "2 100", "2 128", "2 150", 
                                                                     "2 200", "2 250", "2 400", 
                                                                     "2 500", "2 800", "2 1000", 
                                                                     "2 1500", "2 2000", "2 2500"}, 
                                                                    {"4", "8", "10", "16", "20", 
                                                                     "32", "64", "100", "128", 
                                                                     "150", "200", "250", "400", 
                                                                     "500", "800", "1000", 
                                                                     "1500", "2000", "2500"}};
    String[][] executeScheduleRecurringNoOpThreadCases = new String[][]{{"0 4", "0 8", "0 10", "0 16", 
                                                                         "0 20", "0 32", "0 64", 
                                                                         "0 100", "0 128", "0 150", 
                                                                         "0 200", "0 250", "0 400", 
                                                                         "0 500", "0 800", "0 1000", 
                                                                         "0 1500", "0 2000", "0 2500"}, 
                                                                        {"NoOp4", "NoOp8", "NoOp10", 
                                                                         "NoOp16", "NoOp20", 
                                                                         "NoOp32", "NoOp64", 
                                                                         "NoOp100", "NoOp128", 
                                                                         "NoOp150", "NoOp200", 
                                                                         "NoOp250", "NoOp400", 
                                                                         "NoOp500", "NoOp800", 
                                                                         "NoOp1000", "NoOp1500", 
                                                                         "NoOp2000", "NoOp2500"}};
    
    int benchmarkGroup = 0; // incremented for each one
    int classGroup = 0; // incremented at each class change
    
    // java.util baseline
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                JavaUtilConcurrentExecutorExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                JavaUtilConcurrentExecutorExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, // we count all java.util.concurrent in the same class group
                                JavaUtilConcurrentSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup,
                                JavaUtilConcurrentSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                JavaUtilConcurrentSchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                JavaUtilConcurrentSchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                JavaUtilConcurrentSchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                JavaUtilConcurrentSchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    if (! INCLUDE_JAVA_BASELINE) {
      // we must clear so that the benchmarkGroup and classGroup are still what we expect
      toRun.clear();
    }
    
    // top level schedulers
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                PrioritySchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    
    // increment for skipped UnfairExecutor benchmarks
    classGroup++;
    benchmarkGroup += 2;
    
    // increment for skipped SingleThreadScheduler benchmarks
    classGroup++;
    benchmarkGroup += 6;
    
    // skip NoThreadScheduler benchmark
    benchmarkGroup++;
    
    // scheduler wrappers
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                SubmitterSchedulerLimiterExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SubmitterSchedulerLimiterExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SubmitterSchedulerLimiterRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SubmitterSchedulerLimiterRecurringBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SubmitterSchedulerLimiterScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SubmitterSchedulerLimiterScheduleBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    // skip KeyedLimiter benchmarks
    benchmarkGroup += 8;
    classGroup++;
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                KeyDistributedSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    // skip UnfairExecutor backed KeyDistributedExecutor benchmarks
    benchmarkGroup += 2;
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedSchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedSchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedSchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedSchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorSimpleBenchmark.class, 
                                new String[][]{{"true", "false"}, 
                                               {"Execute", "Schedule"}}));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorManySubmitterBenchmark.class, 
                                new String[][]{{"false 4", "false 8", "false 16", "false 32", 
                                                "false 64", "false 200"}, 
                                               {"Execute4", "Execute8", "Execute16", "Execute32", 
                                                "Execute64", "Execute200"}}));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorManySubmitterBenchmark.class, 
                                new String[][]{{"true 4", "true 8", "true 16", "true 32", 
                                                "true 64", "true 200"}, 
                                               {"Schedule4", "Schedule8", "Schedule16", "Schedule32", 
                                                "Schedule64", "Schedule200"}}));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorUniqueKeyBenchmark.class, 
                                new String[][]{{"false 2", "false 4", "false 8", "false 16", 
                                                "false 32", "false 64"}, 
                                               {"Execute2",  "Execute4",  "Execute8", "Execute16", 
                                                "Execute32", "Execute64"}}));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorUniqueKeyBenchmark.class, 
                                new String[][]{{"true 2",  "true 4",  "true 8", "true 16", 
                                                "true 32", "true 64"}, 
                                               {"Schedule2", "Schedule4", "Schedule8", "Schedule16", 
                                                "Schedule32", "Schedule64"}}));
    // skip UnfairExecutor backed KeyDistributedExecutor benchmarks
    benchmarkGroup++;
    
    // Statistic trackers
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                PrioritySchedulerStatisticTrackerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerStatisticTrackerExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerStatisticTrackerRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerStatisticTrackerRecurringBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerStatisticTrackerScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                PrioritySchedulerStatisticTrackerScheduleBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    // skip SingleThreadSchedulerStatisticTracker benchmarks
    benchmarkGroup += 6;
    classGroup++;
    
    toRun.trimToSize();
    BENCHMARKS_TO_RUN = Collections.unmodifiableList(toRun);
  }
  
  private static void printUsage() {
    System.err.println("java -cp <classpath> " + BenchmarkCollectionRunner.class + 
                         " <classpath> <path_to_source_dir> [<dbHost> <dbUser> <dbPass> <dbName>]");
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Must provide at a minimum the classpath used for benchmarks, " + 
                           "and the source directory that is being tested");
      printUsage();
      System.exit(-1);
    }
    String benchmarkClasspath = args[0];
    File sourceFolder = new File(args[1]);
    if (! sourceFolder.exists() || ! sourceFolder.isDirectory()) {
      System.err.println("Provide valid source directory: " + args[1]);
      printUsage();
      System.exit(-2);
    }

    String[] gitCommitCommand = {SHELL, "-c", 
                                 "cd " + sourceFolder.getAbsolutePath() + " ; git log | head -n 1"};
    ExecResult gitCommitResult = runCommand(gitCommitCommand);
    String commitPrefix = "commit ";
    if (! gitCommitResult.stdErr.isEmpty() || 
        gitCommitResult.stdOut.isEmpty() || ! gitCommitResult.stdOut.startsWith(commitPrefix)) {
      System.err.println("Unable to detect git commit");
      System.err.println(gitCommitResult.stdErr);
      System.err.println(gitCommitResult.stdOut);
      System.exit(1);
    }
    final String hash = gitCommitResult.stdOut.substring(commitPrefix.length());
    
    String[] gitBranchCommand = {SHELL, "-c", 
                                 "cd " + sourceFolder.getAbsolutePath() + " ; git rev-parse --abbrev-ref HEAD"};
    ExecResult gitBranchResult = runCommand(gitBranchCommand);
    if (! gitBranchResult.stdErr.isEmpty() || gitBranchResult.stdOut.isEmpty()) {
      System.err.println("Unable to detect git branch");
      System.err.println(gitCommitResult.stdErr);
      System.err.println(gitCommitResult.stdOut);
      System.exit(2);
    }
    final String branchName;
    if (gitBranchResult.stdOut.equals("HEAD")) {
      String[] gitTagCommand = {SHELL, "-c", 
                                "cd " + sourceFolder.getAbsolutePath() + " ; git describe --abbrev=0 --tags"};
      ExecResult gitTagResult = runCommand(gitTagCommand);
      if (! gitTagResult.stdErr.isEmpty() || gitTagResult.stdOut.isEmpty()) {
        System.err.println("Unable to detect git tag");
        System.err.println(gitTagResult.stdErr);
        System.err.println(gitTagResult.stdOut);
        System.exit(3);
      }
      branchName = gitTagResult.stdOut;
    } else {
      branchName = gitBranchResult.stdOut;
    }
    
    System.out.println("Testing '" + branchName + "' hash: '" + hash + "'");
    
    DBI dbi = null;
    if (args.length == 6) {
      Class.forName("org.postgresql.Driver");
      dbi = new DBI("jdbc:postgresql://" + args[2] + '/' + args[5],
                    args[3], args[4]);
    }
    for (final BenchmarkCase bc : BENCHMARKS_TO_RUN) {
      final BenchmarkResult[] results = new BenchmarkResult[bc.executionArgs[0].length];
      for (int i = 0; i < bc.executionArgs[0].length; i++) {
        results[i] = runBenchmarkSet(benchmarkClasspath, 
                                     bc.benchmarkClass, bc.executionArgs[0][i]);
        if (results[i].errorOutput.isEmpty()) {
          String ident = bc.benchmarkClass.getSimpleName() + bc.executionArgs[1][i] + ": ";
          long executionsPerSecond = (results[i].resultValue / (AbstractBenchmark.RUN_TIME / 1000));
          while (ident.length() < 60) {
            ident += " ";
          }
          System.out.println(ident + executionsPerSecond);
        } else {
          System.out.println("Error in running test: " + 
                               bc.benchmarkClass.getSimpleName() + bc.executionArgs[1][i]);
          System.out.println(results[i].errorOutput);
          if (EXIT_ON_BENCHMARK_FAILURE) {
            System.exit(5);
          }
        }
      }
      if (dbi != null) {
        dbi.inTransaction(new TransactionCallback<Void>() {
          @Override
          public Void inTransaction(Handle h, TransactionStatus arg1) throws Exception {
            BenchmarkDao benchmarkDbi = h.attach(BenchmarkDao.class);
            
            int nextBenchmarkGroupRunId = 1 + benchmarkDbi.getLastBenchmarkGroupRunId(bc.benchmarkGroup);
            
            for (int i = 0; i < results.length; i++) {
              BenchmarkResult br = results[i];
              if (br.errorOutput.isEmpty()) {
                String ident = bc.benchmarkClass.getSimpleName().replaceAll("Benchmark", "") + 
                                 bc.executionArgs[1][i];
                benchmarkDbi.addRecord(bc.benchmarkGroup, bc.classGroup, nextBenchmarkGroupRunId, 
                                       hash, branchName, ident, results[i].resultValue, 
                                       AbstractBenchmark.RUN_TIME);
              }
            }
            
            return null;
          }
        });
      }
    }
  }
  
  private static BenchmarkResult runBenchmarkSet(String classpath, 
                                                 Class<? extends AbstractBenchmark> benchmarkClass, 
                                                 String executionArgs) {
    List<Long> runResults = new ArrayList<>(RUN_COUNT);
    for (int i = 0; i < RUN_COUNT; i++) {
      BenchmarkResult br = runBenchmark(classpath, benchmarkClass, executionArgs);
      if (! br.errorOutput.isEmpty()) {
        return br;
      }
      if (i > 0 || ! DISCARD_FIRST_RUN) {
        runResults.add(br.resultValue);
      }
    }
    
    long total = 0;
    for (long i : runResults) {
      total += i;
    }
    return new BenchmarkResult(total / runResults.size());
  }
  
  private static BenchmarkResult runBenchmark(String classpath, 
                                              Class<? extends AbstractBenchmark> benchmarkClass, 
                                              String executionArgs) {
    String[] command = {SHELL, "-c", 
                        JAVA_EXECUTE_CMD + "-cp " + classpath + ' ' + 
                          benchmarkClass.getName() + ' ' + executionArgs};
    System.gc();
    try {
      ExecResult runResult = runCommand(command);
      if (runResult.stdErr.isEmpty()) {
        int delimIndex = runResult.stdOut.indexOf(AbstractBenchmark.OUTPUT_DELIM);
        if (delimIndex > 0) {
          delimIndex += AbstractBenchmark.OUTPUT_DELIM.length();
          long runVal = Long.parseLong(runResult.stdOut.substring(delimIndex));
          return new BenchmarkResult(runVal);
        } else {
          return new BenchmarkResult("Invalid benchmark output: " + runResult.stdOut);
        }
      } else {
        return new BenchmarkResult(runResult.stdErr);
      }
    } catch (Exception e) {
      return new BenchmarkResult(ExceptionUtils.stackToString(e));
    }
  }
  
  private static ExecResult runCommand(String[] command) throws IOException, InterruptedException {
    Process p = null;
    ListenableFuture<String> stdOutFuture = null;
    ListenableFuture<String> stdErrFuture = null;
    try {
      p = Runtime.getRuntime().exec(command);
      stdOutFuture = SCHEDULER.submit(new StreamReader(p.getInputStream()));
      stdErrFuture = SCHEDULER.submit(new StreamReader(p.getErrorStream()));
      
      try {
        return new ExecResult(p.waitFor(), stdOutFuture.get(), stdErrFuture.get());
      } catch (ExecutionException e) {
        throw ExceptionUtils.makeRuntime(e.getCause());
      }
    } catch (IOException | InterruptedException e) {
      if (p != null) {
        p.destroy();
      }
      if (stdOutFuture != null) {
        stdOutFuture.cancel(true);
      }
      if (stdErrFuture != null) {
        stdErrFuture.cancel(true);
      }
      throw e;
    }
  }
  
  private static class BenchmarkResult {
    public final long resultValue;
    public final String errorOutput;
    
    public BenchmarkResult(long resultValue) {
      this.resultValue = resultValue;
      this.errorOutput = "";
    }
    
    public BenchmarkResult(String errorOutput) {
      this.resultValue = -1;
      this.errorOutput = errorOutput == null ? "" : errorOutput.trim();
    }
  }
  
  protected static class BenchmarkCase {
    public final int benchmarkGroup;
    public final int classGroup;
    public final Class<? extends AbstractBenchmark> benchmarkClass;
    public final String[][] executionArgs;
    
    public BenchmarkCase(int benchmarkGroup, int classGroup, 
                         Class<? extends AbstractBenchmark> benchmarkClass, 
                         String[] executionArgs) {
      this(benchmarkGroup, classGroup, benchmarkClass, 
           new String[][]{executionArgs, executionArgs});
    }
    
    public BenchmarkCase(int benchmarkGroup, int classGroup, 
                         Class<? extends AbstractBenchmark> benchmarkClass, 
                         String[][] executionArgs) {
      this.benchmarkGroup = benchmarkGroup;
      this.classGroup = classGroup;
      this.benchmarkClass = benchmarkClass;
      this.executionArgs = executionArgs;
    }
  }
  
  public static class ExecResult {
    public final int code;
    public final String stdOut;
    public final String stdErr;
    
    public ExecResult(int code, String stdOut, String stdErr) {
      this.code = code;
      this.stdOut = stdOut == null ? "" : stdOut.trim();
      this.stdErr = stdErr == null ? "" : stdErr.trim();
    }
  }
  
  private static class StreamReader implements Callable<String> {
    private final InputStream stream;
    
    public StreamReader(InputStream in) {
      stream = in;
    }
    
    @Override
    public String call() throws IOException {
      StringBuilder sb = new StringBuilder();
      byte[] buffer = new byte[1024];
      int c;
      while ((c = stream.read(buffer)) >= 0) {
        sb.append(new String(buffer, 0, c));
      }
      return sb.toString();
    }
  }
}
