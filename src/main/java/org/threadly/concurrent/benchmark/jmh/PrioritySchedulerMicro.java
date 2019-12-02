package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.threadly.concurrent.AbstractPriorityScheduler;
import org.threadly.concurrent.DoNothingRunnable;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.util.Clock;

@Threads(4)
@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class PrioritySchedulerMicro extends AbstractPriorityScheduler {
  private static final TaskWrapper ACCURATE_READY_TASK = 
      new AccurateBenchmarkTaskWrapper(Clock.lastKnownForwardProgressingMillis());
  private static final TaskWrapper ACCURATE_UNREADY_TASK = 
      new AccurateBenchmarkTaskWrapper(Clock.lastKnownTimeMillis());
  private static final TaskWrapper GUESS_READY_RECURRING_TASK = 
      new GuessRecurringBenchmarkTaskWrapper(Clock.lastKnownForwardProgressingMillis(), 0);
  private static final PriorityScheduler SCHEDULER = new PriorityScheduler(1);
  
  @Benchmark
  public void readyTaskGetScheduleDelay() {
    ACCURATE_READY_TASK.getScheduleDelay();
  }
  
  @Benchmark
  public void readyRecurringTaskGetScheduleDelay() {
    GUESS_READY_RECURRING_TASK.getScheduleDelay();
  }
  
  // can't be grouped because this will update Clock, causing inconsistent volatile read performance
  @Benchmark
  public void unreadyTaskGetScheduleDelay() {
    ACCURATE_UNREADY_TASK.getScheduleDelay();
  }
  
  @Benchmark
  public void submitTask() {
    SCHEDULER.schedule(DoNothingRunnable.instance(), 1000, TaskPriority.High);
    SCHEDULER.remove(DoNothingRunnable.instance());
    SCHEDULER.schedule(DoNothingRunnable.instance(), 1000, TaskPriority.Starvable);
    SCHEDULER.remove(DoNothingRunnable.instance());
  }
  
  // inner classes to provide visibility
  
  protected static class AccurateBenchmarkTaskWrapper extends AccurateOneTimeTaskWrapper {
    public AccurateBenchmarkTaskWrapper(long runTime) {
      super(DoNothingRunnable.instance(), null, runTime);
    }
  }
  
  protected static class GuessBenchmarkTaskWrapper extends GuessOneTimeTaskWrapper {
    public GuessBenchmarkTaskWrapper(long runTime) {
      super(DoNothingRunnable.instance(), null, runTime);
    }
  }
  
  protected static class GuessRecurringBenchmarkTaskWrapper extends GuessRecurringRateTaskWrapper {
    public GuessRecurringBenchmarkTaskWrapper(long runTime, long period) {
      super(DoNothingRunnable.instance(), null, runTime, period);
    }
  }
  
  // Below functions are for extending AbstractPriorityScheduler
  // We extend so we can get access to the inner classes
  
  public PrioritySchedulerMicro() {
    super(null);
  }

  @Override
  public void scheduleWithFixedDelay(Runnable task, long initialDelay, long recurringDelay,
                                     TaskPriority priority) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void scheduleAtFixedRate(Runnable task, long initialDelay, long period,
                                  TaskPriority priority) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getActiveTaskCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isShutdown() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected OneTimeTaskWrapper doSchedule(Runnable task, long delayInMillis,
                                          TaskPriority priority) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected QueueManager getQueueManager() {
    throw new UnsupportedOperationException();
  }
}
