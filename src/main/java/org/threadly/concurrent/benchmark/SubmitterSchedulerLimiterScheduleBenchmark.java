package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.limiter.SimpleSchedulerLimiter;

public class SubmitterSchedulerLimiterScheduleBenchmark extends AbstractSchedulerScheduleBenchmark {
  public static void main(String args[]) {
    try {
      new SubmitterSchedulerLimiterScheduleBenchmark(Integer.parseInt(args[0]), 
                                                     Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final PriorityScheduler originalExecutor;
  protected final SubmitterSchedulerInterface executor;
  
  public SubmitterSchedulerLimiterScheduleBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new PriorityScheduler(poolSize, TaskPriority.High, 0);
    originalExecutor.prestartAllThreads();
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
