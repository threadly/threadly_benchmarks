package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.TaskSchedulerDistributor;

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
  protected final TaskSchedulerDistributor keyScheduler;
  
  public KeyDistributedSchedulerRecurringBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new PriorityScheduler(poolSize, poolSize, 10_000, TaskPriority.High, 0);
    originalExecutor.prestartAllCoreThreads();
    keyScheduler = new TaskSchedulerDistributor(originalExecutor);
  }

  @Override
  protected SubmitterSchedulerInterface getScheduler() {
    return keyScheduler.getSubmitterSchedulerForKey(new Object());
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
