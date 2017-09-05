package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PrioritySchedulerStatisticTracker;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;

public class PrioritySchedulerStatisticTrackerExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new PrioritySchedulerStatisticTrackerExecuteBenchmark(Integer.parseInt(args[0]), 
                                                            Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final PrioritySchedulerStatisticTracker scheduler;
  
  public PrioritySchedulerStatisticTrackerExecuteBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    scheduler = new PrioritySchedulerStatisticTracker(poolSize, poolSize, 10_000, TaskPriority.High, 0);
    scheduler.prestartAllCoreThreads();
  }

  @Override
  protected SubmitterSchedulerInterface getScheduler() {
    return scheduler;
  }

  @Override
  protected void shutdownScheduler() {
    scheduler.shutdownNow();
  }
}
