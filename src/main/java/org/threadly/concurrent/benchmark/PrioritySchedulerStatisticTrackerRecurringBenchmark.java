package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PrioritySchedulerStatisticTracker;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;

public class PrioritySchedulerStatisticTrackerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  public static void main(String args[]) {
    try {
      new PrioritySchedulerStatisticTrackerRecurringBenchmark(Integer.parseInt(args[0]), 
                                                              Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final PrioritySchedulerStatisticTracker scheduler;
  
  public PrioritySchedulerStatisticTrackerRecurringBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    scheduler = new PrioritySchedulerStatisticTracker(poolSize, TaskPriority.High, 0);
    scheduler.prestartAllThreads();
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
