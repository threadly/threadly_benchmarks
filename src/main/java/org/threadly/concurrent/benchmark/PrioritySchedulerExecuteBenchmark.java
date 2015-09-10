package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;

public class PrioritySchedulerExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  protected static final PriorityScheduler ORIGINAL_EXECUTOR;
  
  static {
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    ORIGINAL_EXECUTOR = new PriorityScheduler(POOL_SIZE, TaskPriority.High, 0);
    ORIGINAL_EXECUTOR.prestartAllThreads();
  }
  
  public static void main(String args[]) {
    try {
      new PrioritySchedulerExecuteBenchmark().runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return ORIGINAL_EXECUTOR;
  }

  @Override
  protected void shutdownScheduler() {
    ORIGINAL_EXECUTOR.shutdownNow();
  }
}
