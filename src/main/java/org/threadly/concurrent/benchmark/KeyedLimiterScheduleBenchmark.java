package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.limiter.KeyedSubmitterSchedulerLimiter;

public class KeyedLimiterScheduleBenchmark extends AbstractSchedulerScheduleBenchmark {
  public static void main(String args[]) {
    try {
      new KeyedLimiterScheduleBenchmark(Integer.parseInt(args[0])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final PriorityScheduler originalExecutor;
  protected final KeyedSubmitterSchedulerLimiter keyLimiter;
  
  public KeyedLimiterScheduleBenchmark(int poolSize) {
    originalExecutor = new PriorityScheduler(poolSize, TaskPriority.High, 0);
    originalExecutor.prestartAllThreads();
    keyLimiter = new KeyedSubmitterSchedulerLimiter(originalExecutor, Integer.MAX_VALUE);
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return keyLimiter.getSubmitterSchedulerForKey(new Object());
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
