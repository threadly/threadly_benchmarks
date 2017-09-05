package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SingleThreadScheduler;
import org.threadly.concurrent.SubmitterSchedulerInterface;

public class SingleThreadSchedulerExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {

  protected static final SingleThreadScheduler EXECUTOR;
  
  static {
    EXECUTOR = new SingleThreadScheduler();
  }
  
  public static void main(String args[]) {
    try {
      new SingleThreadSchedulerExecuteBenchmark(Integer.parseInt(args[0])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected SingleThreadSchedulerExecuteBenchmark(int threadRunTime) {
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
