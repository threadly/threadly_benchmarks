package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.threadly.concurrent.NoThreadScheduler;
import org.threadly.concurrent.SingleThreadScheduler;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class SingleThreadSchedulerMicro {
  private static final TestableSingleThreadScheduler SCHEDULER = new TestableSingleThreadScheduler();
  
  protected static class TestableSingleThreadScheduler extends SingleThreadScheduler {
    @Override
    public NoThreadScheduler getRunningScheduler() throws RejectedExecutionException {
      return super.getRunningScheduler();
    }
  }

  @Benchmark
  public void getRunningScheduler() {
    SCHEDULER.getRunningScheduler();
  }
}
