package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
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
  private static final WorkerPool NOT_RUNNING_WORKER_POOL = new QueueUpdateIgnoringWorkerPool(null, 1, null) {
    @Override
    public void start(QueueManager queueManager) {
      // no-op
    }
  };
  private static final ThreadFactory SINGLE_THREAD_FACTORY = new ThreadFactory() {
    private final Thread t = new Thread();

    @Override
    public Thread newThread(Runnable arg0) {
      return t;
    }
  };
  // segmented by priority to avoid interactions within groups
  private static final WorkerPool SETUP_HIGH_WORKER_POOL;
  private static final WorkerPool SETUP_LOW_WORKER_POOL;
  private static final WorkerPool SETUP_STARVABLE_WORKER_POOL;
  private static final TestVisibilityWorkerPool IDLE_WORKER_QUEUE_POOL;
  private static final TestVisibilityWorkerPool IDLE_WORKER_QUEUE_CONCURRENT_POOL;
  private static final TestVisibilityWorkerPool IDLE_WORKER_QUEUE_CYCLE_ONE_POOL;
  private static final TestVisibilityWorkerPool IDLE_WORKER_QUEUE_CYCLE_TWO_POOL;
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
    SETUP_HIGH_WORKER_POOL = new QueueUpdateIgnoringWorkerPool(SINGLE_THREAD_FACTORY, 1, TaskPriority.Low);
    SETUP_LOW_WORKER_POOL = new QueueUpdateIgnoringWorkerPool(SINGLE_THREAD_FACTORY, 1, TaskPriority.Low);
    SETUP_STARVABLE_WORKER_POOL = new QueueUpdateIgnoringWorkerPool(SINGLE_THREAD_FACTORY, 1, TaskPriority.Low);
    IDLE_WORKER_QUEUE_POOL = new TestVisibilityWorkerPool(SINGLE_THREAD_FACTORY, 1, TaskPriority.Low, 128);
    IDLE_WORKER_QUEUE_CONCURRENT_POOL = new TestVisibilityWorkerPool(SINGLE_THREAD_FACTORY, 1, TaskPriority.Low, 128);
    IDLE_WORKER_QUEUE_CYCLE_ONE_POOL = new TestVisibilityWorkerPool(SINGLE_THREAD_FACTORY, 1, TaskPriority.Low, 1);
    IDLE_WORKER_QUEUE_CYCLE_TWO_POOL = new TestVisibilityWorkerPool(SINGLE_THREAD_FACTORY, 1, TaskPriority.Low, 2);
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
    NOT_STARTED_HIGH_WORKER = new Worker(SETUP_HIGH_WORKER_POOL, SINGLE_THREAD_FACTORY);
    NOT_STARTED_LOW_WORKER = new Worker(SETUP_LOW_WORKER_POOL, SINGLE_THREAD_FACTORY);
    NOT_STARTED_STARVABLE_WORKER = new Worker(SETUP_STARVABLE_WORKER_POOL, SINGLE_THREAD_FACTORY);
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
  
  @Benchmark
  public void workerCycle_addWorkerToIdleChain() {
    IDLE_WORKER_QUEUE_POOL.loopedWorkerAddToIdleChain();
  }
  
  @Benchmark
  @GroupThreads(4)
  public void workerCycle_addWorkerToIdleChainConcurrent() {
    IDLE_WORKER_QUEUE_CONCURRENT_POOL.loopedWorkerAddToIdleChain();
  }
  
  @Benchmark
  public void workerCycle_addAndRemoveOneWorkerToIdleChain() {
    IDLE_WORKER_QUEUE_CYCLE_ONE_POOL.cycleOneWorker();
  }
  
  @Benchmark
  public void workerCycle_addAndRemoveTwoWorkersToIdleChain() {
    IDLE_WORKER_QUEUE_CYCLE_TWO_POOL.cycleTwoWorkers();
  }

  // constructor so test can be built, but we can gain visibility to protected implementations
  public PrioritySchedulerWorkerPoolMicro() {
    super(NOT_RUNNING_WORKER_POOL, null, 200);
  }
  
  protected static class QueueUpdateIgnoringWorkerPool extends WorkerPool {
    private boolean firstUpdate = true;
    
    public QueueUpdateIgnoringWorkerPool(ThreadFactory threadFactory, int poolSize, 
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
  
  protected static class TestVisibilityWorkerPool extends WorkerPool {
    private final Worker[] workers;
    private final AtomicInteger currIndex = new AtomicInteger();
    
    public TestVisibilityWorkerPool(ThreadFactory threadFactory, int poolSize, 
                                    TaskPriority minStartPriority, int workerQty) {
      super(threadFactory, poolSize, minStartPriority == TaskPriority.Starvable);
      
      workers = new Worker[workerQty];
      for (int i = 0; i < workers.length; i++) {
        workers[i] = new Worker(this, SINGLE_THREAD_FACTORY);
      }
    }
    
    public void cycleOneWorker() {
      super.addWorkerToIdleChain(workers[0]);
      super.removeWorkerFromIdleChain(workers[0]);
    }
    
    public void cycleTwoWorkers() {
      super.addWorkerToIdleChain(workers[0]);
      super.addWorkerToIdleChain(workers[1]);
      super.removeWorkerFromIdleChain(workers[0]);
      super.removeWorkerFromIdleChain(workers[1]);
    }

    public void loopedWorkerAddToIdleChain() {
      int index;
      while (true) {
        index = currIndex.get();
        if (currIndex.compareAndSet(index, (index + 1) % workers.length)) {
          break;
        }
      }
      
      super.addWorkerToIdleChain(workers[index]);
    }
  }
}
