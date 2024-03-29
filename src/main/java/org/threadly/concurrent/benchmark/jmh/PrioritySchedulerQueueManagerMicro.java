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
import org.threadly.concurrent.benchmark.jmh.PrioritySchedulerWorkerPoolMicro.QueueUpdateIgnoringWorkerPool;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class PrioritySchedulerQueueManagerMicro extends PriorityScheduler {
  private static final WorkerPool NOT_RUNNING_WORKER_POOL = new QueueUpdateIgnoringWorkerPool(null, 1, null) {
    @Override
    public void start(QueueManager queueManager) {
      // no-op
    }
  };
  // segmented by priority to avoid interactions within groups
  private static final QueueManager HIGH_QUEUE_MANAGER;
  private static final QueueManager LOW_QUEUE_MANAGER;
  private static final QueueManager STARVABLE_QUEUE_MANAGER;
  
  static {
    ThreadFactory threadFactory = new ConfigurableThreadFactory("Benchmark-", true);
    {
      WorkerPool highPriorityWorkerPool = 
          new QueueUpdateIgnoringWorkerPool(threadFactory, 10, TaskPriority.Low);
      HIGH_QUEUE_MANAGER = new QueueManager(highPriorityWorkerPool, 100);
      highPriorityWorkerPool.start(HIGH_QUEUE_MANAGER);
      QueueSet highPriorityQueueSet = HIGH_QUEUE_MANAGER.getQueueSet(TaskPriority.High);
      QueueSet lowPriorityQueueSet = HIGH_QUEUE_MANAGER.getQueueSet(TaskPriority.Low);
      highPriorityQueueSet.addExecute(
          new ImmediateTaskWrapper(DoNothingRunnable.instance(), 
                                   ProtectedAccessor.executeQueue(highPriorityQueueSet)));
      lowPriorityQueueSet.addExecute(
          new ImmediateTaskWrapper(DoNothingRunnable.instance(), 
                                   ProtectedAccessor.executeQueue(lowPriorityQueueSet)));
    }
    {
      WorkerPool lowPriorityWorkerPool = 
          new QueueUpdateIgnoringWorkerPool(threadFactory, 10, TaskPriority.Low);
      LOW_QUEUE_MANAGER = new QueueManager(lowPriorityWorkerPool, 100);
      lowPriorityWorkerPool.start(LOW_QUEUE_MANAGER);
      QueueSet highPriorityQueueSet = LOW_QUEUE_MANAGER.getQueueSet(TaskPriority.High);
      QueueSet lowPriorityQueueSet = LOW_QUEUE_MANAGER.getQueueSet(TaskPriority.Low);
      highPriorityQueueSet.addExecute(
          new GuessOneTimeTaskWrapper(DoNothingRunnable.instance(), 
                                      ProtectedAccessor.executeQueue(highPriorityQueueSet), 
                                      TimeUnit.DAYS.toMillis(14)));
      lowPriorityQueueSet.addExecute(
          new ImmediateTaskWrapper(DoNothingRunnable.instance(), 
                                   ProtectedAccessor.executeQueue(lowPriorityQueueSet)));
    }
    {
      WorkerPool starvablePriorityWorkerPool = 
          new QueueUpdateIgnoringWorkerPool(threadFactory, 10, TaskPriority.Low);
      STARVABLE_QUEUE_MANAGER = new QueueManager(starvablePriorityWorkerPool, 100);
      starvablePriorityWorkerPool.start(STARVABLE_QUEUE_MANAGER);
      QueueSet highPriorityQueueSet = STARVABLE_QUEUE_MANAGER.getQueueSet(TaskPriority.High);
      QueueSet lowPriorityQueueSet = STARVABLE_QUEUE_MANAGER.getQueueSet(TaskPriority.Low);
      QueueSet starvablePriorityQueueSet = STARVABLE_QUEUE_MANAGER.getQueueSet(TaskPriority.Starvable);
      highPriorityQueueSet.addExecute(
          new GuessOneTimeTaskWrapper(DoNothingRunnable.instance(), 
                                      ProtectedAccessor.executeQueue(highPriorityQueueSet), 
                                      TimeUnit.DAYS.toMillis(14)));
      lowPriorityQueueSet.addExecute(
          new GuessOneTimeTaskWrapper(DoNothingRunnable.instance(), 
                                      ProtectedAccessor.executeQueue(lowPriorityQueueSet), 
                                      TimeUnit.DAYS.toMillis(14)));
      starvablePriorityQueueSet.addExecute(
          new ImmediateTaskWrapper(DoNothingRunnable.instance(), 
                                   ProtectedAccessor.executeQueue(starvablePriorityQueueSet)));
    }
  }
  
  @Benchmark
  public void getTaskHigh_next() {
    HIGH_QUEUE_MANAGER.getNextTask(true);
  }
  
  @Benchmark
  public void getTaskHigh_nextShortcut() {
    HIGH_QUEUE_MANAGER.getNextTask(false);
  }
  
  @Benchmark
  public void getTaskLow_next() {
    LOW_QUEUE_MANAGER.getNextTask(true);
  }
  
  @Benchmark
  public void getTaskLow_nextShortcut() {
    LOW_QUEUE_MANAGER.getNextTask(false);
  }
  
  /*@Benchmark
  // can't be grouped due to clock check / update
  public void getTaskStarvable_next() {
    STARVABLE_QUEUE_MANAGER.getNextTask(true);
    //STARVABLE_QUEUE_MANAGER.getNextAnyTask();
  }*/
  
  @Benchmark
  public void getTaskStarvable_nextShortcut() {
    STARVABLE_QUEUE_MANAGER.getNextTask(false);
    //STARVABLE_QUEUE_MANAGER.getNextNonStarvableTask();
  }

  // constructor so test can be built, but we can gain visibility to protected implementations
  public PrioritySchedulerQueueManagerMicro() {
    super(NOT_RUNNING_WORKER_POOL, null, 200);
  }
}
