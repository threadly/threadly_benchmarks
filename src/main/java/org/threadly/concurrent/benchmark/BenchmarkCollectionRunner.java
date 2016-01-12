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
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.benchmark.dao.BenchmarkDao;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.util.ExceptionUtils;
import org.threadly.util.StringUtils;

public class BenchmarkCollectionRunner {
  protected static final int RUN_COUNT = 5;
  protected static final boolean INCLUDE_JAVA_BASELINE = false;
  protected static final boolean EXIT_ON_BENCHMARK_FAILURE = false;
  protected static final boolean DISCARD_FIRST_RUN = false;
  protected static final String SHELL = "bash";
  protected static final String JAVA_EXECUTE_CMD = "/usr/bin/java -Xmx2560m -Xms2048m -Xss256k ";
  protected static final PriorityScheduler SCHEDULER;
  protected static final List<BenchmarkCase> BENCHMARKS_TO_RUN;
  
  static {
    SCHEDULER = new PriorityScheduler(2);
    SCHEDULER.prestartAllThreads();
    SCHEDULER.setPoolSize(10);
    
    ArrayList<BenchmarkCase> toRun = new ArrayList<>();
    
    String[][] executeScheduleRecurringThreadCases = new String[][]{{"2 4", "2 10", "2 20", "2 50", 
                                                                     "2 100", "2 150", "2 200", "2 250", 
                                                                     "2 500", "2 750", "2 1000", 
                                                                     "2 1500", "2 2000", "2 2500"}, 
                                                                    {"4", "10", "20", "50", 
                                                                     "100", "150", "200", "250", 
                                                                     "500", "750", "1000", 
                                                                     "1500", "2000", "2500"}};
    String[][] executeScheduleRecurringNoOpThreadCases = new String[][]{{"0 4", "0 10", "0 20", "0 50", 
                                                                         "0 100", "0 150", "0 200", "0 250", 
                                                                         "0 500", "0 750", "0 1000", 
                                                                         "0 1500", "0 2000", "0 2500"}, 
                                                                        {"NoOp4", "NoOp10", "NoOp20", "NoOp50", 
                                                                         "NoOp100", "NoOp150", "NoOp200", "NoOp250", 
                                                                         "NoOp500", "NoOp750", "NoOp1000", 
                                                                         "NoOp1500", "NoOp2000", "NoOp2500"}};
    
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
    String[][] singleThreadSchedulerArgs = new String[][] {{"2"}, {""}};
    String[][] singleThreadSchedulerArgsNpOp = new String[][] {{"0"}, {"NoOp"}};
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                SingleThreadSchedulerExecuteBenchmark.class, 
                                singleThreadSchedulerArgs));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SingleThreadSchedulerExecuteBenchmark.class, 
                                singleThreadSchedulerArgsNpOp));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SingleThreadSchedulerRecurringBenchmark.class, 
                                singleThreadSchedulerArgs));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SingleThreadSchedulerRecurringBenchmark.class, 
                                singleThreadSchedulerArgsNpOp));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SingleThreadSchedulerScheduleBenchmark.class, 
                                singleThreadSchedulerArgs));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                SingleThreadSchedulerScheduleBenchmark.class, 
                                singleThreadSchedulerArgsNpOp));
    // since NoThreadScheduler is the basis of SingleThreadScheduler they are in the same class group
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                NoThreadSchedulerBenchmark.class, new String[]{"10", "50"}));
    
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
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                KeyedLimiterExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyedLimiterExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyedLimiterRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyedLimiterRecurringBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyedLimiterScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyedLimiterScheduleBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, ++classGroup, 
                                KeyDistributedSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringNoOpThreadCases));
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
                                new String[][]{{"false 10", "false 50", "false 100", "false 200"}, 
                                               {"Execute10",  "Execute50",  "Execute100",  "Execute200"}}));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorManySubmitterBenchmark.class, 
                                new String[][]{{"true 10",  "true 50",  "true 100",  "true 200"}, 
                                               {"Schedule10", "Schedule50", "Schedule100", "Schedule200"}}));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorUniqueKeyBenchmark.class, 
                                new String[][]{{"false 2", "false 4", "false 8", "false 16"}, 
                                               {"Execute2",  "Execute4",  "Execute8", "Execute16"}}));
    toRun.add(new BenchmarkCase(++benchmarkGroup, classGroup, 
                                KeyDistributedExecutorUniqueKeyBenchmark.class, 
                                new String[][]{{"true 2",  "true 4",  "true 8",  "true 16"}, 
                                               {"Schedule2", "Schedule4", "Schedule8", "Schedule16"}}));
    
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
    if (! StringUtils.isNullOrEmpty(gitCommitResult.stdErr) || 
        StringUtils.isNullOrEmpty(gitCommitResult.stdOut) || ! gitCommitResult.stdOut.startsWith(commitPrefix)) {
      System.err.println("Unable to detect git commit");
      System.err.println(gitCommitResult.stdErr);
      System.err.println(gitCommitResult.stdOut);
      System.exit(1);
    }
    final String hash = gitCommitResult.stdOut.substring(commitPrefix.length());
    
    String[] gitBranchCommand = {SHELL, "-c", 
                                 "cd " + sourceFolder.getAbsolutePath() + " ; git rev-parse --abbrev-ref HEAD"};
    ExecResult gitBranchResult = runCommand(gitBranchCommand);
    if (! StringUtils.isNullOrEmpty(gitBranchResult.stdErr) || StringUtils.isNullOrEmpty(gitBranchResult.stdOut)) {
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
      if (! StringUtils.isNullOrEmpty(gitTagResult.stdErr) || StringUtils.isNullOrEmpty(gitTagResult.stdOut)) {
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
        if (StringUtils.isNullOrEmpty(results[i].errorOutput)) {
          String ident = bc.benchmarkClass.getSimpleName() + bc.executionArgs[1][i] + ": ";
          long executionsPerSecond = (results[i].resultValue / (AbstractBenchmark.RUN_TIME / 1000));
          System.out.println(StringUtils.padEnd(ident, 60, ' ') + executionsPerSecond);
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
              if (StringUtils.isNullOrEmpty(br.errorOutput)) {
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
    
    double total = 0;
    for (long i : runResults) {
      total += i;
    }
    return new BenchmarkResult((long)(total / runResults.size()));
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
      if (StringUtils.isNullOrEmpty(runResult.stdErr)) {
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
      this.errorOutput = StringUtils.nullToEmpty(errorOutput);
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
      this.stdOut = StringUtils.nullToEmpty(stdOut).trim();
      this.stdErr = StringUtils.nullToEmpty(stdErr).trim();
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
