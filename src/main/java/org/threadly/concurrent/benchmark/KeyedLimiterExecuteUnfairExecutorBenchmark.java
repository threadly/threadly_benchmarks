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
  
  public KeyedLimiterExecuteUnfairExecutorBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new UnfairExecutor(poolSize);
    keyLimiter = new KeyedExecutorLimiter(originalExecutor, Integer.MAX_VALUE);
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return new ExecutorSchedulerAdapter(keyLimiter.getSubmitterExecutorForKey(new Object()));
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
