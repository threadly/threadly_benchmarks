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
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.concurrent.future.SettableListenableFuture;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class SettableListenableFutureMicro extends AbstractListenableFutureMicro {
  private static final SettableListenableFuture<Void> SETTABLE_DONE_WITH_RESULT;
  private static final SettableListenableFuture<Void> SETTABLE_DONE_WITH_FAILURE;
  
  static {
    SETTABLE_DONE_WITH_RESULT = new SettableListenableFuture<>();
    SETTABLE_DONE_WITH_RESULT.setResult(null);
    SETTABLE_DONE_WITH_FAILURE = new SettableListenableFuture<>();
    SETTABLE_DONE_WITH_FAILURE.setFailure(FAILURE);
  }
  
  @Benchmark
  public void set_result() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.setResult(null);
  }
  
  @Benchmark
  public void set_failure() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void doneResult_listener() {
    SETTABLE_DONE_WITH_RESULT.listener(DoNothingRunnable.instance());
  }
  
  @Benchmark
  public void doneResult_resultCallback() {
    SETTABLE_DONE_WITH_RESULT.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  public void doneResult_failureCallbackNoOp() {
    SETTABLE_DONE_WITH_RESULT.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  public void doneFailure_resultCallbackNoOp() {
    SETTABLE_DONE_WITH_FAILURE.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  public void doneFailure_failureCallback() {
    SETTABLE_DONE_WITH_FAILURE.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  public void doneResultMap_mapped() {
    SETTABLE_DONE_WITH_RESULT.map(MAPPRER);
  }
  
  @Benchmark
  public void doneResultMap_executedMapped() {
    SETTABLE_DONE_WITH_RESULT.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void doneResultMap_failureMapNoOp() {
    SETTABLE_DONE_WITH_RESULT.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  public void doneResultMap_failureMapExecutedNoOp() {
    SETTABLE_DONE_WITH_RESULT.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                         SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void doneFailureMap_mapped() {
    SETTABLE_DONE_WITH_FAILURE.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  public void doneFailureMap_executedMapped() {
    SETTABLE_DONE_WITH_FAILURE.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                          SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void doneFailureMap_mapNoOp() {
    SETTABLE_DONE_WITH_FAILURE.map(MAPPRER);
  }
  
  @Benchmark
  public void doneFailureMap_executedMapNoOp() {
    SETTABLE_DONE_WITH_FAILURE.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void setResultListener_1Called() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.listener(DoNothingRunnable.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void setResultListener_1Executed() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.listener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void setResultListener_2Called() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.listener(DoNothingRunnable.instance());
    slf.listener(DoNothingRunnable.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void setResultListener_4Called() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.listener(DoNothingRunnable.instance());
    slf.listener(DoNothingRunnable.instance());
    slf.listener(DoNothingRunnable.instance());
    slf.listener(DoNothingRunnable.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void setResultResultCallback_1Called() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK);
    slf.setResult(null);
  }
  
  @Benchmark
  public void setResultResultCallback_1Executed() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void setResultFailureCallback_noOp() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.failureCallback(FAILURE_CALLBACK);
    slf.setResult(null);
  }
  
  @Benchmark
  public void setResultFailureCallback_executedNoOp() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.failureCallback(FAILURE_CALLBACK, SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void setFailureResultCallback_noOp() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void setFailureResultCallback_executedNoOp() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void setFailureFailureCallback_1Called() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.failureCallback(FAILURE_CALLBACK);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void setFailureFailureCallback_1Executed() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.failureCallback(FAILURE_CALLBACK, SameThreadSubmitterExecutor.instance());
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void mapResult_mappedSetResult() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER);
    slf.setResult(null);
  }
  
  @Benchmark
  public void mapResult_executedMappedSetResult() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void mapFailure_noOpSetResult() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER);
    slf.setResult(null);
  }
  
  @Benchmark
  public void mapFailure_executedNoOpSetResult() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  public void mapResult_noOpSetFailure() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void mapResult_executedNoOpSetFailure() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void mapFailure_mappedSetFailure() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  public void mapFailure_executedMappedSetFailure() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    slf.setFailure(FAILURE);
  }
}
