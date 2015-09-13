package org.threadly.concurrent.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.util.ExceptionUtils;
import org.threadly.util.StringUtils;

public class BenchmarkCollectionRunner {
  private static final int RUN_COUNT = 2;//5;
  private static final boolean DISCARD_FIRST_RUN = false;
  private static final String JAVA_EXECUTE_CMD = "/usr/bin/java -Xmx2560m -Xms2048m -Xss256k ";
  private static final PriorityScheduler SCHEDULER;
  private static final List<BenchmarkCase> BENCHMARKS_TO_RUN;
  
  static {
    SCHEDULER = new PriorityScheduler(2);
    SCHEDULER.prestartAllThreads();
    SCHEDULER.setPoolSize(10);
    
    ArrayList<BenchmarkCase> toRun = new ArrayList<>();
    
    String[] noArgs = new String[]{""};
    String[] executeScheduleRecurringThreadCases = new String[]{"10", "50", "100", "200", "500"};
    
    // java.util baseline
    toRun.add(new BenchmarkCase(JavaUtilConcurrentExecutorExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(JavaUtilConcurrentSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(JavaUtilConcurrentSchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(JavaUtilConcurrentSchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    
    // top level schedulers
    toRun.add(new BenchmarkCase(PrioritySchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(PrioritySchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(PrioritySchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(SingleThreadSchedulerExecuteBenchmark.class, noArgs));
    toRun.add(new BenchmarkCase(SingleThreadSchedulerRecurringBenchmark.class, noArgs));
    toRun.add(new BenchmarkCase(SingleThreadSchedulerScheduleBenchmark.class, noArgs));
    toRun.add(new BenchmarkCase(NoThreadSchedulerBenchmark.class, new String[]{"10", "50"}));
    
    // scheduler wrappers
    toRun.add(new BenchmarkCase(SubmitterSchedulerLimiterExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(SubmitterSchedulerLimiterRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(SubmitterSchedulerLimiterScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(KeyedLimiterExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(KeyedLimiterRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(KeyedLimiterScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(KeyDistributedSchedulerExecuteBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(KeyDistributedSchedulerRecurringBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(KeyDistributedSchedulerScheduleBenchmark.class, 
                                executeScheduleRecurringThreadCases));
    toRun.add(new BenchmarkCase(KeyDistributedExecutorSimpleBenchmark.class, 
                                new String[][]{new String[]{"true", "false"}, 
                                               new String[]{"Execute", "Schedule"}}));
    toRun.add(new BenchmarkCase(KeyDistributedExecutorManySubmitterBenchmark.class, 
                                new String[][]{new String[]{"false 10", "false 50", "false 100", "false 200", 
                                                            "true 10",  "true 50",  "true 100",  "true 200"}, 
                                               new String[]{"Execute10",  "Execute50",  "Execute100",  "Execute200",
                                                            "Schedule10", "Schedule50", "Schedule100", "Schedule200"}}));
    toRun.add(new BenchmarkCase(KeyDistributedExecutorUniqueKeyBenchmark.class, 
                                new String[][]{new String[]{"false 2", "false 4", "false 8", "false 16", 
                                                            "true 2",  "true 4",  "true 8",  "true 16"}, 
                                               new String[]{"Execute2",  "Execute4",  "Execute8", "Execute16", 
                                                            "Schedule2", "Schedule4", "Schedule8", "Schedule16"}}));
    
    toRun.trimToSize();
    BENCHMARKS_TO_RUN = Collections.unmodifiableList(toRun);
  }
  
  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO - verify arguments and print helpful messages
    String benchmarkClasspath = args[0];
    File sourceFolder = new File(args[1]);
    if (! sourceFolder.exists() || ! sourceFolder.isDirectory()) {
      System.err.println("Provide valid source directory: " + args[1]);
      System.exit(-1);
    }

    String[] gitCommand = {"/bin/bash", "-c", 
                           "cd " + sourceFolder.getAbsolutePath() + " ; git log | head -n 1"};
    ExecResult gitResult = runCommand(gitCommand);
    String commitPrefix = "commit ";
    if (! StringUtils.isNullOrEmpty(gitResult.stdErr) || 
        StringUtils.isNullOrEmpty(gitResult.stdOut) || ! gitResult.stdOut.startsWith(commitPrefix)) {
      System.err.println("Unable to detect git commit");
      System.err.println(gitResult.stdErr);
      System.err.println(gitResult.stdOut);
      System.exit(1);
    }
    String hash = gitResult.stdOut.substring(commitPrefix.length());
    System.out.println("Testing hash: '" + hash + "'");
    
    for (BenchmarkCase bc : BENCHMARKS_TO_RUN) {
      for (int i = 0; i < bc.executionArgs[0].length; i++) {
        BenchmarkResult result = runBenchmarkSet(benchmarkClasspath, 
                                                 bc.benchmarkClass, 
                                                 bc.executionArgs[0][i]);
        if (StringUtils.isNullOrEmpty(result.errorOutput)) {
          String ident = bc.benchmarkClass.getSimpleName() + bc.executionArgs[1][i] + ": ";
          System.out.println(StringUtils.padEnd(ident, 60, ' ') + result.resultValue);
        } else {
          System.out.println("Error in running test: " + 
                               bc.benchmarkClass.getSimpleName() + bc.executionArgs[1][i]);
          System.out.println(result.errorOutput);
          System.exit(2);
        }
        // TODO - report results with hash
      }
    }
  }
  
  private static BenchmarkResult runBenchmarkSet(String classpath, 
                                                 Class<? extends AbstractBenchmark> benchmarkClass, 
                                                 String executionArgs) {
    List<Integer> runResults = new ArrayList<>(RUN_COUNT);
    for (int i = 0; i < RUN_COUNT; i++) {
      BenchmarkResult br = runBenchmark(classpath, benchmarkClass, executionArgs);
      if (! br.errorOutput.isEmpty()) {
        return br;
      }
      if (i > 0 || ! DISCARD_FIRST_RUN) {
        runResults.add(br.resultValue);
      }
    }
    
    int total = 0;
    for (int i : runResults) {
      total += i;
    }
    return new BenchmarkResult(benchmarkClass, total / runResults.size());
  }
  
  private static BenchmarkResult runBenchmark(String classpath, 
                                              Class<? extends AbstractBenchmark> benchmarkClass, 
                                              String executionArgs) {
    String[] command = {"/bin/bash", "-c", 
                        JAVA_EXECUTE_CMD + "-cp " + classpath + ' ' + 
                          benchmarkClass.getName() + ' ' + executionArgs};
    try {
      ExecResult runResult = runCommand(command);
      if (StringUtils.isNullOrEmpty(runResult.stdErr)) {
        int delimIndex = runResult.stdOut.indexOf(AbstractBenchmark.OUTPUT_DELIM);
        if (delimIndex > 0) {
          delimIndex += AbstractBenchmark.OUTPUT_DELIM.length();
          int runVal = Integer.parseInt(runResult.stdOut.substring(delimIndex));
          return new BenchmarkResult(benchmarkClass, runVal);
        } else {
          return new BenchmarkResult(benchmarkClass, "Invalid benchmark output: " + runResult.stdOut);
        }
      } else {
        return new BenchmarkResult(benchmarkClass, runResult.stdErr);
      }
    } catch (Exception e) {
      return new BenchmarkResult(benchmarkClass, ExceptionUtils.stackToString(e));
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
        p.destroyForcibly();
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
    // TODO - should we record the class tested here or not?
    public final Class<? extends AbstractBenchmark> benchmarkClass;
    public final int resultValue;
    public final String errorOutput;
    
    public BenchmarkResult(Class<? extends AbstractBenchmark> benchmarkClass, int resultValue) {
      this.benchmarkClass = benchmarkClass;
      this.resultValue = resultValue;
      this.errorOutput = "";
    }
    
    public BenchmarkResult(Class<? extends AbstractBenchmark> benchmarkClass, String errorOutput) {
      this.benchmarkClass = benchmarkClass;
      this.resultValue = -1;
      this.errorOutput = StringUtils.nullToEmpty(errorOutput);
    }
  }
  
  private static class BenchmarkCase {
    public final Class<? extends AbstractBenchmark> benchmarkClass;
    public final String[][] executionArgs;
    
    public BenchmarkCase(Class<? extends AbstractBenchmark> benchmarkClass, 
                         String[] executionArgs) {
      this(benchmarkClass, new String[][]{executionArgs, executionArgs});
    }
    
    public BenchmarkCase(Class<? extends AbstractBenchmark> benchmarkClass, 
                         String[][] executionArgs) {
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
