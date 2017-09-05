package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduledExecutor;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;

public class PrioritySchedulerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  public static void main(String args[]) {
    try {
      new PrioritySchedulerRecurringBenchmark(Integer.parseInt(args[0]), 
                                              Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final PriorityScheduledExecutor scheduler;
  
  public PrioritySchedulerRecurringBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    scheduler = new PriorityScheduledExecutor(poolSize, poolSize, 10_000, TaskPriority.High, 0);
    scheduler.prestartAllCoreThreads();
  }

  @Override
  protected SubmitterSchedulerInterface getScheduler() {
    return scheduler;
  }

  @Override
  protected void shutdownScheduler() {
    scheduler.shutdownNow();
  }
}
