package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
  private static final ImmediateResultListenableFuture<Void> RESULT_FUTURE = 
      new ImmediateResultListenableFuture<>(null);
  private static final ImmediateFailureListenableFuture<Void> FAILURE_FUTURE = 
      new ImmediateFailureListenableFuture<>(FAILURE);

  @Benchmark
  public void listener_called() {
    RESULT_FUTURE.listener(DoNothingRunnable.instance());
  }
  
  @Benchmark
  public void listener_executed() {
    RESULT_FUTURE.listener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void resultCallback_called() {
    RESULT_FUTURE.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  public void resultCallback_executed() {
    RESULT_FUTURE.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void map_resultMapped() {
    RESULT_FUTURE.map(MAPPRER);
  }
  
  @Benchmark
  public void map_resultMapExecuted() {
    RESULT_FUTURE.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void map_failureMapped() {
    FAILURE_FUTURE.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  public void map_failureMappedExecuted() {
    FAILURE_FUTURE.mapFailure(Exception.class, FAILURE_MAPPRER, SameThreadSubmitterExecutor.instance());
  }
}
