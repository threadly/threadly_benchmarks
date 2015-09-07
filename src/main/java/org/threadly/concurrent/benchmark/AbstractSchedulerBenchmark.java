package org.threadly.concurrent.benchmark;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.threadly.concurrent.SubmitterScheduler;

public abstract class AbstractSchedulerBenchmark extends AbstractBenchmark {
  protected static final int POOL_SIZE = 100;
  protected static final int RUNNABLE_COUNT = 10000;
  protected static final int RUNNABLE_ADD_TIME = 1000 * 5 * 1;
  protected static final int THREAD_RUN_TIME = 20;
  protected static final boolean USE_JAVA_EXECUTOR = false;
  protected static final boolean RUN_PROFILER = false;
  
  protected static final ScheduledExecutorService JAVA_EXECUTOR;
  
  static {
    ScheduledThreadPoolExecutor jExecutor = new ScheduledThreadPoolExecutor(POOL_SIZE);
    if (USE_JAVA_EXECUTOR) {
      jExecutor.prestartAllCoreThreads();
    }
    JAVA_EXECUTOR = jExecutor;
    //JAVA_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
  }
  
  protected abstract SubmitterScheduler getScheduler();
  
  protected abstract void shutdownScheduler();
}
