package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.statistics.SingleThreadSchedulerStatisticTracker;

public class SingleThreadSchedulerStatisticTrackerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  protected static final SingleThreadSchedulerStatisticTracker EXECUTOR;
  
  static {
    EXECUTOR = new SingleThreadSchedulerStatisticTracker();
  }
  
  public static void main(String args[]) {
    try {
      new SingleThreadSchedulerStatisticTrackerRecurringBenchmark(Integer.parseInt(args[0])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected SingleThreadSchedulerStatisticTrackerRecurringBenchmark(int threadRunTime) {
    super(threadRunTime);
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return EXECUTOR;
  }

  @Override
  protected void shutdownScheduler() {
    EXECUTOR.shutdownNow();
  }
}
