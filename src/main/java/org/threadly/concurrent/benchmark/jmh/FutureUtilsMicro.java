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
import org.threadly.concurrent.future.FutureUtils;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class FutureUtilsMicro {
  @Benchmark
  @Group("Immediate")
  public void futureUtilsImmediateResultFutureNull() {
    FutureUtils.immediateResultFuture(null);
  }
  
  @Benchmark
  @Group("Immediate")
  public void futureUtilsImmediateResultFutureEmptyString() {
    FutureUtils.immediateResultFuture("");
  }
  
  @Benchmark
  @Group("Immediate")
  public void futureUtilsImmediateResultFutureBooleanFalse() {
    FutureUtils.immediateResultFuture(false);
  }
  
  @Benchmark
  @Group("Immediate")
  public void futureUtilsImmediateResultFutureBooleanTrue() {
    FutureUtils.immediateResultFuture(true);
  }
  
  @Benchmark
  @Group("Immediate")
  public void futureUtilsImmediateResultFutureArbitrayObject() {
    FutureUtils.immediateResultFuture(this);
  }
}
