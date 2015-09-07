package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.KeyDistributedScheduler;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;

public class KeyDistributedSchedulerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  protected static final PriorityScheduler ORIGINAL_EXECUTOR;
  
  static {
    ORIGINAL_EXECUTOR = new PriorityScheduler(POOL_SIZE, TaskPriority.High, 0);
    if (! USE_JAVA_EXECUTOR) {
      ORIGINAL_EXECUTOR.prestartAllThreads();
    }
  }
  
  public static void main(String args[]) {
    try {
      new KeyDistributedSchedulerRecurringBenchmark().runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return new KeyDistributedScheduler(ORIGINAL_EXECUTOR)
             .getSubmitterSchedulerForKey(new Object());
  }

  @Override
  protected void shutdownScheduler() {
    ORIGINAL_EXECUTOR.shutdownNow();
  }
}
