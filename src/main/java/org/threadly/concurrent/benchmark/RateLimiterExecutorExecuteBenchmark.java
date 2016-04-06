package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.wrapper.limiter.RateLimiterExecutor;

public class RateLimiterExecutorExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new RateLimiterExecutorExecuteBenchmark(Integer.parseInt(args[0]), 
                                              Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final PriorityScheduler originalExecutor;
  protected final SubmitterScheduler executor;
  
  public RateLimiterExecutorExecuteBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new PriorityScheduler(poolSize);
    executor = new ExecutorSchedulerAdapter(new RateLimiterExecutor(originalExecutor, 
                                                                    Double.MAX_VALUE));
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
