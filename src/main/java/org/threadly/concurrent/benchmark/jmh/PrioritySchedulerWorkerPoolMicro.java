package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.threadly.concurrent.ConfigurableThreadFactory;
import org.threadly.concurrent.DoNothingRunnable;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.ProtectedAccessor;
import org.threadly.concurrent.TaskPriority;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class PrioritySchedulerWorkerPoolMicro extends PriorityScheduler {
  private static final WorkerPool NOT_RUNNING_WORKER_POOL = new QueuUpdateIgnoringWorkerPool(null, 1, null) {
    @Override
    public void start(QueueManager queueManager) {
      // no-op
    }
  };
  // segmented by priority to avoid interactions within groups
  private static final WorkerPool SETUP_HIGH_WORKER_POOL;
  private static final WorkerPool SETUP_LOW_WORKER_POOL;
  private static final WorkerPool SETUP_STARVABLE_WORKER_POOL;
  private static final QueueManager SETUP_HIGH_QUEUE_MANAGER;
  private static final QueueManager SETUP_LOW_QUEUE_MANAGER;
  private static final QueueManager SETUP_STARVABLE_QUEUE_MANAGER;
  private static final QueueSet HIGH_PRIORITY_QUEUE_SET;
  private static final QueueSet LOW_PRIORITY_QUEUE_SET;
  private static final QueueSet STARVABLE_PRIORITY_QUEUE_SET;
  private static final ImmediateTaskWrapper HIGH_PRIORITY_TASK;
  private static final ImmediateTaskWrapper LOW_PRIORITY_TASK;
  private static final ImmediateTaskWrapper STARVABLE_PRIORITY_TASK;
  private static final Worker NOT_STARTED_HIGH_WORKER;
  private static final Worker NOT_STARTED_LOW_WORKER;
  private static final Worker NOT_STARTED_STARVABLE_WORKER;
  
  static {
    ThreadFactory threadFactory = new ConfigurableThreadFactory("Benchmark-", true);
    SETUP_HIGH_WORKER_POOL = new QueuUpdateIgnoringWorkerPool(threadFactory, 1, TaskPriority.Low);
    SETUP_LOW_WORKER_POOL = new QueuUpdateIgnoringWorkerPool(threadFactory, 1, TaskPriority.Low);
    SETUP_STARVABLE_WORKER_POOL = new QueuUpdateIgnoringWorkerPool(threadFactory, 1, TaskPriority.Low);
    SETUP_HIGH_QUEUE_MANAGER = new QueueManager(SETUP_HIGH_WORKER_POOL, 100);
    SETUP_LOW_QUEUE_MANAGER = new QueueManager(SETUP_LOW_WORKER_POOL, 100);
    SETUP_STARVABLE_QUEUE_MANAGER = new QueueManager(SETUP_STARVABLE_WORKER_POOL, 100);
    HIGH_PRIORITY_QUEUE_SET = SETUP_HIGH_QUEUE_MANAGER.getQueueSet(TaskPriority.High);
    LOW_PRIORITY_QUEUE_SET = SETUP_LOW_QUEUE_MANAGER.getQueueSet(TaskPriority.Low);
    STARVABLE_PRIORITY_QUEUE_SET = SETUP_STARVABLE_QUEUE_MANAGER.getQueueSet(TaskPriority.Starvable);
    HIGH_PRIORITY_TASK = new ImmediateTaskWrapper(DoNothingRunnable.instance(), 
                                                  ProtectedAccessor.executeQueue(HIGH_PRIORITY_QUEUE_SET));
    LOW_PRIORITY_TASK = new ImmediateTaskWrapper(DoNothingRunnable.instance(), 
                                                 ProtectedAccessor.executeQueue(LOW_PRIORITY_QUEUE_SET));
    STARVABLE_PRIORITY_TASK = new ImmediateTaskWrapper(DoNothingRunnable.instance(), 
                                                       ProtectedAccessor.executeQueue(STARVABLE_PRIORITY_QUEUE_SET));
    SETUP_HIGH_WORKER_POOL.start(SETUP_HIGH_QUEUE_MANAGER);
    SETUP_LOW_WORKER_POOL.start(SETUP_LOW_QUEUE_MANAGER);
    SETUP_STARVABLE_WORKER_POOL.start(SETUP_STARVABLE_QUEUE_MANAGER);
    NOT_STARTED_HIGH_WORKER = new Worker(SETUP_HIGH_WORKER_POOL, threadFactory);
    NOT_STARTED_LOW_WORKER = new Worker(SETUP_LOW_WORKER_POOL, threadFactory);
    NOT_STARTED_STARVABLE_WORKER = new Worker(SETUP_STARVABLE_WORKER_POOL, threadFactory);
  }
  
  @Benchmark
  public void taskCycle_addAndPollHighPriority() {
    ProtectedAccessor.resetStatus(HIGH_PRIORITY_TASK);
    HIGH_PRIORITY_QUEUE_SET.addExecute(HIGH_PRIORITY_TASK);
    SETUP_HIGH_WORKER_POOL.workerIdle(NOT_STARTED_HIGH_WORKER);
  }
  
  @Benchmark
  public void taskCycle_addAndPollLowPriority() {
    ProtectedAccessor.resetStatus(LOW_PRIORITY_TASK);
    LOW_PRIORITY_QUEUE_SET.addExecute(LOW_PRIORITY_TASK);
    SETUP_LOW_WORKER_POOL.workerIdle(NOT_STARTED_LOW_WORKER);
  }
  
  @Benchmark
  public void taskCycle_addAndPollStarvablePriority() {
    ProtectedAccessor.resetStatus(STARVABLE_PRIORITY_TASK);
    STARVABLE_PRIORITY_QUEUE_SET.addExecute(STARVABLE_PRIORITY_TASK);
    SETUP_STARVABLE_WORKER_POOL.workerIdle(NOT_STARTED_STARVABLE_WORKER);
  }

  // constructor so test can be built, but we can gain visibility to protected implementations
  public PrioritySchedulerWorkerPoolMicro() {
    super(NOT_RUNNING_WORKER_POOL, null, 200);
  }
  
  protected static class QueuUpdateIgnoringWorkerPool extends WorkerPool {
    private boolean firstUpdate = true;
    
    public QueuUpdateIgnoringWorkerPool(ThreadFactory threadFactory, int poolSize, 
                                        TaskPriority minStartPriority) {
      super(threadFactory, poolSize, minStartPriority == TaskPriority.Starvable);
    }
    
    @Override
    public void handleQueueUpdate() {
      if (firstUpdate) {
        currentPoolSize.set(Math.max(1, this.getMaxPoolSize() / 2)); // lie about pool size
        firstUpdate = false;
      }
    }
  }
}
