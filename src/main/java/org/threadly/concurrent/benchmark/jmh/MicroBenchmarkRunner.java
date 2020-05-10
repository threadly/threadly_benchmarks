package org.threadly.concurrent.benchmark.jmh;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.threadly.concurrent.benchmark.AbstractBenchmark;
import org.threadly.util.ExceptionUtils;
import org.threadly.util.StringUtils;

public class MicroBenchmarkRunner {
  public static final int FORKS = 2;
  public static final int WARMUP_ITERATIONS = 5;
  public static final int WARMUP_SECONDS = 2;
  public static final int RUN_ITERATIONS = 5;
  public static final int RUN_SECONDS = 10;
  private static final boolean ALLOW_MIXED_GROUP_EXECUTION = false;
  private static final int MIN_EXPECTED_CORES = 8;
  private static final int MAX_GROUP_SIZE = 
      (ALLOW_MIXED_GROUP_EXECUTION ? Runtime.getRuntime().availableProcessors() : MIN_EXPECTED_CORES) / 2;
  private static final OutputFormat NORMAL_OUTPUT = 
      OutputFormatFactory.createFormatInstance(System.out, VerboseMode.NORMAL);
  private static final OutputFormat SILENT_OUTPUT = 
      OutputFormatFactory.createFormatInstance(System.out, VerboseMode.SILENT);
  private static final int GROUP_RUN_SECONDS = 
      FORKS * ((WARMUP_ITERATIONS * WARMUP_SECONDS) + (RUN_ITERATIONS * RUN_SECONDS));
  
  public static void main(String[] args) throws Exception {
    if (Runtime.getRuntime().availableProcessors() < MIN_EXPECTED_CORES) {
      System.err.println("JMH Benchmarks are tuned to run on at least an 8 core machine");
      System.exit(1);
    }
    System.setProperty("jmh.ignoreLock", "true"); // we need to run concurrently ourselves
    
    // parse and remove our custom arguments
    List<String> argList = new ArrayList<>(args.length);
    for (String s : args) {
      argList.add(s);
    }
    final boolean direct = argList.remove("-d"); // force JMH's runner and disable our grouping
    final boolean quiet = argList.remove("-q");  // enable a quiet mode with a -q
    if (argList.size() != args.length) {
      args = new String[argList.size()];
      args = argList.toArray(args);
    }
    
    CommandLineOptions cmdOptions = new CommandLineOptions(args);
    if (cmdOptions.shouldHelp()) {
      cmdOptions.showHelp();
      return;
    } else if (cmdOptions.shouldList()) {
      new Runner(cmdOptions, NORMAL_OUTPUT).list();
      return;
    } else if (cmdOptions.shouldListWithParams()) {
      new Runner(cmdOptions, NORMAL_OUTPUT).listWithParams(cmdOptions);
      return;
    } else if (cmdOptions.shouldListProfilers()) {
      cmdOptions.listProfilers();
      return;
    } else if (cmdOptions.shouldListResultFormats()) {
      cmdOptions.listResultFormats();
      return;
    }
    
    if (direct) {
      runDirectToJMH(quiet, cmdOptions);
    } else {
      runManuallyGrouped(quiet, cmdOptions);
    }
  }
  
  private static void runDirectToJMH(boolean quiet, CommandLineOptions cmdOptions) throws RunnerException {
    Collection<RunResult> results = 
        new Runner(cmdOptions, quiet ? SILENT_OUTPUT : NORMAL_OUTPUT).run();
    
    if (quiet) { // no output from JMH, provide our own
      for (RunResult rr : results) {
        String label = cmdOptions.getIncludes().get(0);
        if (! label.endsWith(rr.getPrimaryResult().getLabel())) {
          label = label + '.' + rr.getPrimaryResult().getLabel();
        }
        
        System.out.printf("%s%s%f\n", 
                          label, AbstractBenchmark.OUTPUT_DELIM, 
                          rr.getPrimaryResult().getScore());
      }
    }
  }
  
  private static String shortNameBenchmark(String benchmark) {
    return benchmark.replace(MicroBenchmarkRunner.class.getPackage().getName() + ".", "");
  }
  
  private static void runManuallyGrouped(boolean quiet, CommandLineOptions cmdOptions) throws InterruptedException {
    Map<String, List<String>> benchmarks = new HashMap<>();
    AtomicInteger maxNameLength = new AtomicInteger(0);
    AtomicInteger totalCount = new AtomicInteger(0);
    BenchmarkList.defaultList()
                 .find(NORMAL_OUTPUT, cmdOptions.getIncludes(), cmdOptions.getExcludes())
                 .forEach((b) -> {
                   totalCount.incrementAndGet();
                   String benchmark = b.getUsername();
                   String group;
                   int groupDelim = benchmark.lastIndexOf('_');
                   if (groupDelim > 0) {
                     group = benchmark.substring(0, groupDelim);
                   } else {
                     group = benchmark;
                   }
                   group = shortNameBenchmark(group);
                   
                   int shortNameLength = shortNameBenchmark(benchmark).length();
                   if (shortNameLength > maxNameLength.get()) {
                     // not thread safe, just need atomic for lambda access
                     maxNameLength.set(shortNameLength);
                   }
                   
                   List<String> groupList = 
                       benchmarks.computeIfAbsent(group, (k) -> new ArrayList<>(8));
                   groupList.add(benchmark);
                   if (groupList.size() > MAX_GROUP_SIZE) {
                     throw new IllegalStateException("Group too large: " + group);
                   }
                 });
    
    double groupDoneCount = 0;  // only used if group isolated execution
    double benchmarkDoneCount = 0;  // only used if mixed group execution
    List<String> results = new ArrayList<>(totalCount.get());
    ArrayDeque<Thread> benchmarkThreads = new ArrayDeque<>(MAX_GROUP_SIZE);
    for (Map.Entry<String, List<String>> group : benchmarks.entrySet()) {
      if (ALLOW_MIXED_GROUP_EXECUTION) {
        if (! quiet) {
          if (benchmarkThreads.isEmpty() || benchmarkThreads.size() == MAX_GROUP_SIZE) {
            int donePercent = (int)((benchmarkDoneCount / totalCount.get()) * 100);
            int remainingGroupEstimate = (int)((totalCount.get() - benchmarkDoneCount) / MAX_GROUP_SIZE) + 1;
            int etaMinutes = (remainingGroupEstimate * GROUP_RUN_SECONDS) / 60;
            System.out.println("# Run progress: " + StringUtils.padStart(Integer.toString(donePercent), 2, '0') + 
                                 "% complete, ETA " + etaMinutes + "min");
          }
          System.out.println("# Starting group " + group.getKey() + " size: " + group.getValue().size());
        }
      } else {
        if (! benchmarkThreads.isEmpty()) {
          for (Thread t : benchmarkThreads) {
            t.join();
          }
          benchmarkThreads.clear();
          groupDoneCount++;
        }
        
        if (! quiet) {
          int donePercent = (int)((groupDoneCount / benchmarks.size()) * 100);
          int etaMinutes = (int)(((benchmarks.size() - groupDoneCount) * GROUP_RUN_SECONDS) / 60);
          System.out.println("# Run progress: " + StringUtils.padStart(Integer.toString(donePercent), 2, '0') + 
                               "% complete, ETA " + etaMinutes + "min");
          System.out.println("# Running group " + group.getKey() + " size: " + group.getValue().size());
        }
      }
      
      for (String benchmark : group.getValue()) {
        if (ALLOW_MIXED_GROUP_EXECUTION) {
          if (benchmarkThreads.size() == MAX_GROUP_SIZE) {
            benchmarkThreads.remove().join();
            benchmarkDoneCount++;
          }
        }
        
        Thread t = new Thread(() -> {
          try {
            Runner bRun = 
                new Runner(
                    new CommandLineOptions(new String[] { benchmark + (benchmark.endsWith("$") ? "" : "$") }), 
                    SILENT_OUTPUT);
            RunResult rr = bRun.runSingle();
            String resultStr;
            String benchmarkShortname = shortNameBenchmark(benchmark);
            if (quiet) {
              resultStr = String.format("%s%s%f", 
                                        benchmarkShortname, AbstractBenchmark.OUTPUT_DELIM, 
                                        rr.getPrimaryResult().getScore());
            } else {
              resultStr = String.format("%s %f\t ± \t%f %s", 
                                        StringUtils.padEnd(benchmarkShortname, maxNameLength.get(), ' '), 
                                        rr.getPrimaryResult().getScore(), 
                                        rr.getPrimaryResult().getScoreError(), 
                                        rr.getPrimaryResult().getScoreUnit());
            }
            synchronized (results) {
              results.add(resultStr);
            }
          } catch (Exception e) {
            throw ExceptionUtils.makeRuntime(e);
          }
        }, group.getKey() + "-" + benchmarkThreads.size());
        benchmarkThreads.add(t);
        t.start();
      }
    }
    
    while (! benchmarkThreads.isEmpty()) {
      benchmarkThreads.remove().join();
    }
    
    Collections.sort(results);
    if (! quiet) {
      System.out.println(StringUtils.padEnd("Benchmark", maxNameLength.get(), ' ') + " Score \t\t± \tError");
    }
    for (String s : results) {
      System.out.println(s);
    }
  }
}
