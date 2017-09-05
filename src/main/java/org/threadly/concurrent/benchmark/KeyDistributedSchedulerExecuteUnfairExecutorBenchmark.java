package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.KeyDistributedExecutor;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.UnfairExecutor;

public class KeyDistributedSchedulerExecuteUnfairExecutorBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new KeyDistributedSchedulerExecuteUnfairExecutorBenchmark(Integer.parseInt(args[0]), 
                                                                Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final UnfairExecutor originalExecutor;
  protected final KeyDistributedExecutor keyScheduler;
  protected final SubmitterScheduler scheduler;
  
  public KeyDistributedSchedulerExecuteUnfairExecutorBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new UnfairExecutor(poolSize);
    keyScheduler = new KeyDistributedExecutor(originalExecutor);
    scheduler = new ExecutorSchedulerAdapter((task) -> keyScheduler.getExecutorForKey(new Object())
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
