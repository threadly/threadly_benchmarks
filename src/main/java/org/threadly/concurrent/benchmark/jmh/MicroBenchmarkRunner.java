package org.threadly.concurrent.benchmark.jmh;

public class MicroBenchmarkRunner {
  public static final int FORKS = 2;
  public static final int WARMUP_ITERATIONS = 5;
  public static final int WARMUP_SECONDS = 2;
  public static final int RUN_ITERATIONS = 5;
  public static final int RUN_SECONDS = 10;
  
  public static void main(String[] args) throws Exception {
    if (Runtime.getRuntime().availableProcessors() < 8) {
      System.err.println("JMH Benchmarks are tuned to run on at least an 8 core machine");
      System.exit(1);
    }
    
    org.openjdk.jmh.Main.main(args);
  }
}
