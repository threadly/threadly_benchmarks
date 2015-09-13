package org.threadly.concurrent.benchmark;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.threadly.concurrent.ScheduledExecutorServiceWrapper;
import org.threadly.concurrent.SubmitterScheduler;

public class JavaUtilConcurrentSchedulerExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new JavaUtilConcurrentSchedulerExecuteBenchmark(Integer.parseInt(args[0])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final ScheduledThreadPoolExecutor originalExecutor;
  protected final SubmitterScheduler executor;
  
  public JavaUtilConcurrentSchedulerExecuteBenchmark(int poolSize) {
    originalExecutor = new ScheduledThreadPoolExecutor(poolSize);
    originalExecutor.prestartAllCoreThreads();
    executor = new ScheduledExecutorServiceWrapper(originalExecutor);
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return executor;
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
