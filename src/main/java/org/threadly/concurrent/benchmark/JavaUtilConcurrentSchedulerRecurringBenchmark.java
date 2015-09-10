package org.threadly.concurrent.benchmark;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.threadly.concurrent.ScheduledExecutorServiceWrapper;
import org.threadly.concurrent.SubmitterScheduler;

public class JavaUtilConcurrentSchedulerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  protected static final ScheduledThreadPoolExecutor ORIGINAL_EXECUTOR;
  protected static final SubmitterScheduler EXECUTOR;
  
  static {
    ORIGINAL_EXECUTOR = new ScheduledThreadPoolExecutor(POOL_SIZE);
    ORIGINAL_EXECUTOR.prestartAllCoreThreads();
    EXECUTOR = new ScheduledExecutorServiceWrapper(ORIGINAL_EXECUTOR);
  }
  
  public static void main(String args[]) {
    try {
      new JavaUtilConcurrentSchedulerRecurringBenchmark().runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return EXECUTOR;
  }

  @Override
  protected void shutdownScheduler() {
    ORIGINAL_EXECUTOR.shutdownNow();
  }
}
