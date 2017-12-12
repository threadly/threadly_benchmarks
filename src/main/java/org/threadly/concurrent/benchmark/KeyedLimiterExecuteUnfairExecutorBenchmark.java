package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.UnfairExecutor;
import org.threadly.concurrent.wrapper.limiter.KeyedExecutorLimiter;

public class KeyedLimiterExecuteUnfairExecutorBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new KeyedLimiterExecuteUnfairExecutorBenchmark(Integer.parseInt(args[0]), 
                                                     Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final UnfairExecutor originalExecutor;
  protected final KeyedExecutorLimiter keyLimiter;
  protected final SubmitterScheduler scheduler;
  
  public KeyedLimiterExecuteUnfairExecutorBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new UnfairExecutor(poolSize);
    keyLimiter = new KeyedExecutorLimiter(originalExecutor, Integer.MAX_VALUE);
    scheduler = new ExecutorSchedulerAdapter((task) -> keyLimiter.getSubmitterExecutorForKey(new Object())
                                                                 .execute(task));
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return scheduler;
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
