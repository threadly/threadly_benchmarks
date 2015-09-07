package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;

public class PrioritySchedulerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  protected static final PriorityScheduler ORIGINAL_EXECUTOR;
  protected static final SubmitterScheduler EXECUTOR;
  
  static {
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    ORIGINAL_EXECUTOR = new PriorityScheduler(POOL_SIZE, TaskPriority.High, 0);
    if (! USE_JAVA_EXECUTOR) {
      ORIGINAL_EXECUTOR.prestartAllThreads();
    }
    //EXECUTOR = ORIGINAL_EXECUTOR.makeSubPool(POOL_SIZE);
    EXECUTOR = ORIGINAL_EXECUTOR;
  }
  
  public static void main(String args[]) {
    try {
      new PrioritySchedulerRecurringBenchmark().runTest();
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
