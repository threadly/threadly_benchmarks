package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SingleThreadScheduler;
import org.threadly.concurrent.SubmitterSchedulerInterface;

public class SingleThreadSchedulerRecurringBenchmark extends AbstractSchedulerRecurringBenchmark {
  protected static final SingleThreadScheduler EXECUTOR;
  
  static {
    EXECUTOR = new SingleThreadScheduler();
  }
  
  public static void main(String args[]) {
    try {
      new SingleThreadSchedulerRecurringBenchmark(Integer.parseInt(args[0])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected SingleThreadSchedulerRecurringBenchmark(int threadRunTime) {
    super(threadRunTime);
  }

  @Override
  protected SubmitterSchedulerInterface getScheduler() {
    return EXECUTOR;
  }

  @Override
  protected void shutdownScheduler() {
    EXECUTOR.shutdownNow();
  }
}
