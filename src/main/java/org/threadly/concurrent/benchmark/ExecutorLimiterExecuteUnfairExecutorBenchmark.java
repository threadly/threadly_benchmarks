package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.UnfairExecutor;
import org.threadly.concurrent.wrapper.limiter.ExecutorLimiter;

public class ExecutorLimiterExecuteUnfairExecutorBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new ExecutorLimiterExecuteUnfairExecutorBenchmark(Integer.parseInt(args[0]), 
                                                        Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected final UnfairExecutor originalExecutor;
  protected final SubmitterScheduler executor;
  
  public ExecutorLimiterExecuteUnfairExecutorBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new UnfairExecutor(poolSize);
    executor = new ExecutorSchedulerAdapter(new ExecutorLimiter(originalExecutor, Integer.MAX_VALUE));
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
