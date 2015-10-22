package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;

public class PrioritySchedulerScheduleBenchmark extends AbstractSchedulerScheduleBenchmark {
  public static void main(String args[]) {
    try {
      new PrioritySchedulerScheduleBenchmark(Integer.parseInt(args[0]), 
                                             Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final PriorityScheduler scheduler;
  
  public PrioritySchedulerScheduleBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    scheduler = new PriorityScheduler(poolSize, TaskPriority.High, 0);
    scheduler.prestartAllThreads();
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return scheduler;
  }

  @Override
  protected void shutdownScheduler() {
    scheduler.shutdownNow();
  }
}
