package org.threadly.concurrent.benchmark;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.threadly.concurrent.SubmitterSchedulerInterface;

public class JavaUtilConcurrentExecutorExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new JavaUtilConcurrentExecutorExecuteBenchmark(Integer.parseInt(args[0]), 
                                                     Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final ThreadPoolExecutor originalExecutor;
  protected final SubmitterSchedulerInterface executor;
  
  public JavaUtilConcurrentExecutorExecuteBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new ThreadPoolExecutor(poolSize, poolSize, 
                                              Integer.MAX_VALUE, TimeUnit.MILLISECONDS, 
                                              new ArrayBlockingQueue<Runnable>(RUNNABLE_COUNT));
    originalExecutor.prestartAllCoreThreads();
    executor = new ExecutorSchedulerAdapter(originalExecutor);
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
