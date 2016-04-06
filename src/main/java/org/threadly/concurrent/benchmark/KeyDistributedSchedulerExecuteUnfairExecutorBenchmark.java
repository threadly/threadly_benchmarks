package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.UnfairExecutor;
import org.threadly.concurrent.wrapper.KeyDistributedExecutor;

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
  
  public KeyDistributedSchedulerExecuteUnfairExecutorBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new UnfairExecutor(poolSize);
    keyScheduler = new KeyDistributedExecutor(originalExecutor);
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return new ExecutorSchedulerAdapter(keyScheduler.getExecutorForKey(new Object()));
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
