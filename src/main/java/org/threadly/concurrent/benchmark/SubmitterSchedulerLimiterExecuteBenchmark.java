package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduledExecutor;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.limiter.SimpleSchedulerLimiter;

public class SubmitterSchedulerLimiterExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new SubmitterSchedulerLimiterExecuteBenchmark(Integer.parseInt(args[0]), 
                                                    Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final PriorityScheduledExecutor originalExecutor;
  protected final SubmitterSchedulerInterface executor;
  
  public SubmitterSchedulerLimiterExecuteBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new PriorityScheduledExecutor(poolSize, poolSize, 10_000, TaskPriority.High, 0);
    originalExecutor.prestartAllCoreThreads();
    executor = new SimpleSchedulerLimiter(originalExecutor, Integer.MAX_VALUE);
  }
  
  @Override
  protected SubmitterSchedulerInterface getScheduler() {
    return executor;
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
