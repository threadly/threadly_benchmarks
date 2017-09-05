package org.threadly.concurrent.benchmark;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.threadly.concurrent.ScheduledExecutorServiceWrapper;
import org.threadly.concurrent.SubmitterSchedulerInterface;

public class JavaUtilConcurrentSchedulerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  public static void main(String args[]) {
    try {
      new JavaUtilConcurrentSchedulerRecurringBenchmark(Integer.parseInt(args[0]), 
                                                        Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final ScheduledThreadPoolExecutor originalExecutor;
  protected final SubmitterSchedulerInterface executor;
  
  public JavaUtilConcurrentSchedulerRecurringBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new ScheduledThreadPoolExecutor(poolSize);
    originalExecutor.prestartAllCoreThreads();
    executor = new ScheduledExecutorServiceWrapper(originalExecutor);
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
