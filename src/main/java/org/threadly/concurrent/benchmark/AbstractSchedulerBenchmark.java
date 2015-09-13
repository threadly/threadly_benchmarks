package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.SubmitterScheduler;

public abstract class AbstractSchedulerBenchmark extends AbstractBenchmark {
  protected static final int RUNNABLE_COUNT = 1000;
  protected static final int RUNNABLE_ADD_TIME = 1000 * 5 * 1;
  protected static final int THREAD_RUN_TIME = 20;
  protected static final boolean RUN_PROFILER = false;
  
  protected abstract SubmitterScheduler getScheduler();
  
  protected abstract void shutdownScheduler();
}
