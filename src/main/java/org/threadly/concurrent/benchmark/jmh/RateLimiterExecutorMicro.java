package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.threadly.concurrent.DoNothingRunnable;
import org.threadly.concurrent.wrapper.limiter.RateLimiterExecutor;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class RateLimiterExecutorMicro {
  private static final int THREADS_PER_TEST = 2;
  private static final BenchmarkRateLimiterExecutor LIMITER_1 = 
      new BenchmarkRateLimiterExecutor();
  private static final BenchmarkRateLimiterExecutor LIMITER_2 = 
      new BenchmarkRateLimiterExecutor();
  private static final BenchmarkRateLimiterExecutor LIMITER_3 = 
      new BenchmarkRateLimiterExecutor();
  private static final BenchmarkRateLimiterExecutor LIMITER_4 = 
      new BenchmarkRateLimiterExecutor();
  private static final Runnable TEST_DO_NOTHING_RUNNABLE = () -> { /* nothing */ };
  
  @Benchmark
  @GroupThreads(THREADS_PER_TEST)
  @Group("SubmitTask")
  public void submitDoNothingRunnable() {
    LIMITER_1.execute(1, DoNothingRunnable.instance());
  }
  
  /*@Benchmark
  @GroupThreads(THREADS_PER_TEST)
  @Group("SubmitTask")
  public void submitRealRunnable() {
    LIMITER_2.execute(1, TEST_DO_NOTHING_RUNNABLE);
  }*/
  
  @Benchmark
  @GroupThreads(THREADS_PER_TEST)
  @Group("SubmitTask")
  public void submitDoNothingRunnableNoPermit() {
    LIMITER_3.execute(0, DoNothingRunnable.instance());
  }
  
  /*@Benchmark
  @GroupThreads(THREADS_PER_TEST)
  @Group("SubmitTask")
  public void submitRealRunnableNoPermit() {
    LIMITER_4.execute(0, TEST_DO_NOTHING_RUNNABLE);
  }*/
  
  private static class BenchmarkRateLimiterExecutor extends RateLimiterExecutor {
    public BenchmarkRateLimiterExecutor() {
      super(DoNothingSchedulerService.INSTANCE, Double.MAX_VALUE, Long.MAX_VALUE, null);
    }
  }
}
