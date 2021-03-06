package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.wrapper.KeyDistributedScheduler;

public class KeyDistributedSchedulerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  public static void main(String args[]) {
    try {
      new KeyDistributedSchedulerRecurringBenchmark(Integer.parseInt(args[0]), 
                                                    Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final PriorityScheduler originalExecutor;
  protected final KeyDistributedScheduler keyScheduler;
  
  public KeyDistributedSchedulerRecurringBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new PriorityScheduler(poolSize, TaskPriority.High, 0);
    originalExecutor.prestartAllThreads();
    keyScheduler = new KeyDistributedScheduler(originalExecutor);
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return keyScheduler.getSchedulerForKey(new Object());
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
