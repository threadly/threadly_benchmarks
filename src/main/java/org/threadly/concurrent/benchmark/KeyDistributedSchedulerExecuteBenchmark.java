package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.KeyDistributedScheduler;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;

public class KeyDistributedSchedulerExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new KeyDistributedSchedulerExecuteBenchmark(Integer.parseInt(args[0]), 
                                                  Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final PriorityScheduler originalExecutor;
  protected final KeyDistributedScheduler keyScheduler;
  
  public KeyDistributedSchedulerExecuteBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new PriorityScheduler(poolSize, TaskPriority.High, 0);
    originalExecutor.prestartAllThreads();
    keyScheduler = new KeyDistributedScheduler(originalExecutor);
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
