package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.limiter.KeyedSubmitterSchedulerLimiter;

public class KeyedLimiterExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  protected static final PriorityScheduler ORIGINAL_EXECUTOR;
  protected static final KeyedSubmitterSchedulerLimiter KEY_LIMITER;
  
  static {
    ORIGINAL_EXECUTOR = new PriorityScheduler(POOL_SIZE, TaskPriority.High, 0);
    if (! USE_JAVA_EXECUTOR) {
      ORIGINAL_EXECUTOR.prestartAllThreads();
    }
    KEY_LIMITER = new KeyedSubmitterSchedulerLimiter(ORIGINAL_EXECUTOR, Integer.MAX_VALUE);
  }
  
  public static void main(String args[]) {
    try {
      new KeyedLimiterExecuteBenchmark().runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return KEY_LIMITER.getSubmitterSchedulerForKey(new Object());
  }

  @Override
  protected void shutdownScheduler() {
    ORIGINAL_EXECUTOR.shutdownNow();
  }
}
