package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.limiter.SubmitterSchedulerLimiter;

public class SubmitterSchedulerLimiterRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  public static void main(String args[]) {
    try {
      new SubmitterSchedulerLimiterRecurringBenchmark(Integer.parseInt(args[0])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final PriorityScheduler originalExecutor;
  protected final SubmitterScheduler executor;
  
  public SubmitterSchedulerLimiterRecurringBenchmark(int poolSize) {
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    originalExecutor = new PriorityScheduler(poolSize, TaskPriority.High, 0);
    originalExecutor.prestartAllThreads();
    executor = new SubmitterSchedulerLimiter(originalExecutor, Integer.MAX_VALUE);
  }
  
  @Override
  protected SubmitterScheduler getScheduler() {
    return executor;
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
