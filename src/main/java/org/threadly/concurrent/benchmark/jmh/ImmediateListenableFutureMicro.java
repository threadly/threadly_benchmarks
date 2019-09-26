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
import org.threadly.concurrent.future.ImmediateFailureListenableFuture;
import org.threadly.concurrent.future.ImmediateResultListenableFuture;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class ImmediateListenableFutureMicro extends AbstractListenableFutureMicro {
  private static final ImmediateFailureListenableFuture<Void> FAILURE_FUTURE = 
      new ImmediateFailureListenableFuture<>(FAILURE);

  @Benchmark
  @Group("Listener")
  public void resultFutureListener() {
    ImmediateResultListenableFuture.NULL_RESULT.listener(DoNothingRunnable.instance());
  }
  
  @Benchmark
  @Group("Listener")
  public void resultFutureExecutedListener() {
    ImmediateResultListenableFuture.NULL_RESULT.listener(DoNothingRunnable.instance(), 
                                                         SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  @Group("Listener")
  public void resultFutureResultCallback() {
    ImmediateResultListenableFuture.NULL_RESULT.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  @Group("Listener")
  public void resultFutureExecutedResultCallback() {
    ImmediateResultListenableFuture.NULL_RESULT.resultCallback(RESULT_CALLBACK, 
                                                               SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  @Group("Map")
  public void resultFutureMapped() {
    ImmediateResultListenableFuture.NULL_RESULT.map(MAPPRER);
  }
  
  @Benchmark
  @Group("Map")
  public void resultFutureExecutedMapped() {
    ImmediateResultListenableFuture.NULL_RESULT.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  @Group("Map")
  public void failureFutureMapped() {
    FAILURE_FUTURE.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  @Group("Map")
  public void failureFutureExecutedMapped() {
    FAILURE_FUTURE.mapFailure(Exception.class, FAILURE_MAPPRER, SameThreadSubmitterExecutor.instance());
  }
}
