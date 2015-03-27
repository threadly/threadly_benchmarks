package org.threadly.concurrent.benchmark;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.SingleThreadScheduler;
import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.TaskPriority;

@SuppressWarnings("unused")
public abstract class AbstractPrioritySchedulerBenchmark extends AbstractBenchmark {
  protected static final int POOL_SIZE = 100;
  protected static final int RUNNABLE_COUNT = 10000;
  protected static final int RUNNABLE_ADD_TIME = 1000 * 5 * 1;
  protected static final int THREAD_RUN_TIME = 20;
  protected static final boolean USE_JAVA_EXECUTOR = false;
  protected static final boolean RUN_PROFILER = false;
  
  protected static final PriorityScheduler ORIGINAL_EXECUTOR;
  protected static final SubmitterSchedulerInterface EXECUTOR;
  
  static {
    // change to StrictPriorityScheduler for testing logic (and then run inside eclipse)
    ORIGINAL_EXECUTOR = new PriorityScheduler(POOL_SIZE, TaskPriority.High, 0);
    ORIGINAL_EXECUTOR.prestartAllThreads();
    //EXECUTOR = ORIGINAL_EXECUTOR.makeSubPool(POOL_SIZE);
    EXECUTOR = ORIGINAL_EXECUTOR;
    //EXECUTOR = new SingleThreadScheduler();
  }
  
  protected static final ScheduledExecutorService JAVA_EXECUTOR;
  
  static {
    ScheduledThreadPoolExecutor jExecutor = new ScheduledThreadPoolExecutor(POOL_SIZE);
    jExecutor.prestartAllCoreThreads();
    JAVA_EXECUTOR = jExecutor;
    //JAVA_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
  }
}
