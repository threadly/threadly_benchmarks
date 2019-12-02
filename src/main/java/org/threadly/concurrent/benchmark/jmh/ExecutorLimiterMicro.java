package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.threadly.concurrent.DoNothingRunnable;
import org.threadly.concurrent.SameThreadSubmitterExecutor;
import org.threadly.concurrent.wrapper.limiter.ExecutorLimiter;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class ExecutorLimiterMicro {
  private static final BenchmarkExecutorLimiter LIMITER_1 = 
      new BenchmarkExecutorLimiter();
  private static final BenchmarkExecutorLimiter LIMITER_2 = 
      new BenchmarkExecutorLimiter();
  
  @Benchmark
  @Group("AddAndConsume")
  public void consumeOneLimitedTask() {
    LIMITER_1.addTasks(1);
    LIMITER_1.executeTasks();
  }
  
  @Benchmark
  @Group("AddAndConsume")
  public void consumeTwoLimitedTasks() {
    LIMITER_2.addTasks(2);
    LIMITER_2.executeTasks();
  }
  
  private static class BenchmarkExecutorLimiter extends ExecutorLimiter {
    private final LimiterRunnableWrapper doNothingRunnableWrapper = 
        new LimiterRunnableWrapper(DoNothingRunnable.instance());
    
    public BenchmarkExecutorLimiter() {
      super(SameThreadSubmitterExecutor.instance(), 1);
    }

    public void addTasks(int tasks) {
      for (int i = 0; i < tasks; i++) {
        super.waitingTasks.add(doNothingRunnableWrapper);
      }
    }
    
    public void executeTasks() {
      super.consumeAvailable();
    }
  }
}
