package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.KeyDistributedScheduler;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.TaskPriority;

public class KeyDistributedSchedulerScheduleBenchmark extends AbstractSchedulerScheduleBenchmark {
  protected static final PriorityScheduler ORIGINAL_EXECUTOR;
  protected static final SubmitterScheduler EXECUTOR;
  
  static {
    ORIGINAL_EXECUTOR = new PriorityScheduler(POOL_SIZE, TaskPriority.High, 0);
    if (! USE_JAVA_EXECUTOR) {
      ORIGINAL_EXECUTOR.prestartAllThreads();
    }
    EXECUTOR = new KeyDistributedScheduler(ORIGINAL_EXECUTOR).getSubmitterSchedulerForKey("foo");
  }
  
  public static void main(String args[]) {
    try {
      new KeyDistributedSchedulerScheduleBenchmark().runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return EXECUTOR;
  }

  @Override
  protected void shutdownScheduler() {
    ORIGINAL_EXECUTOR.shutdownNow();
  }
}