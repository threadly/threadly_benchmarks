package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.limiter.ExecutorLimiter;

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

  protected final PriorityScheduler originalExecutor;
  protected final SubmitterScheduler executor;
  
  public ExecutorLimiterExecuteUnfairExecutorBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    originalExecutor = new PriorityScheduler(poolSize, TaskPriority.High, 0);
    originalExecutor.prestartAllThreads();
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
