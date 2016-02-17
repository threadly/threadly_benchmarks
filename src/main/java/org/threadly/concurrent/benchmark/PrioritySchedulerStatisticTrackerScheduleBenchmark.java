package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.statistics.PrioritySchedulerStatisticTracker;

public class PrioritySchedulerStatisticTrackerScheduleBenchmark extends AbstractSchedulerScheduleBenchmark {
  public static void main(String args[]) {
    try {
      new PrioritySchedulerStatisticTrackerScheduleBenchmark(Integer.parseInt(args[0]), 
                                                             Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final PrioritySchedulerStatisticTracker scheduler;
  
  public PrioritySchedulerStatisticTrackerScheduleBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    scheduler = new PrioritySchedulerStatisticTracker(poolSize, TaskPriority.High, 0);
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
