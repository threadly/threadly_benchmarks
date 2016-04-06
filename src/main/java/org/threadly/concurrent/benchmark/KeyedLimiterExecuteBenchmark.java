package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.wrapper.limiter.KeyedSubmitterSchedulerLimiter;

public class KeyedLimiterExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new KeyedLimiterExecuteBenchmark(Integer.parseInt(args[0]), 
                                       Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final PriorityScheduler originalExecutor;
  protected final KeyedSubmitterSchedulerLimiter keyLimiter;
  
  public KeyedLimiterExecuteBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
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
