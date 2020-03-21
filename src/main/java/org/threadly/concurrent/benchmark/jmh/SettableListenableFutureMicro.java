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
  @Group("Set")
  public void setResult() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("Set")
  public void setFailure() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  @Group("Done")
  public void doneResultWithListener() {
    SETTABLE_DONE_WITH_RESULT.listener(DoNothingRunnable.instance());
  }
  
  @Benchmark
  @Group("Done")
  public void doneResultWithResultCallback() {
    SETTABLE_DONE_WITH_RESULT.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  @Group("Done")
  public void doneResultWithFailureCallback() {
    SETTABLE_DONE_WITH_RESULT.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  @Group("Done")
  public void doneFailureWithResultCallback() {
    SETTABLE_DONE_WITH_FAILURE.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  @Group("Done")
  public void doneFailureWithFailureCallback() {
    SETTABLE_DONE_WITH_FAILURE.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneResultMapped() {
    SETTABLE_DONE_WITH_RESULT.map(MAPPRER);
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneResultExecutedMapped() {
    SETTABLE_DONE_WITH_RESULT.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneFailureMapped() {
    SETTABLE_DONE_WITH_FAILURE.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneFailureExecutedMapped() {
    SETTABLE_DONE_WITH_FAILURE.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                          SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneFailureMapNoOp() {
    SETTABLE_DONE_WITH_FAILURE.map(MAPPRER);
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneFailureExecutedMapNoOp() {
    SETTABLE_DONE_WITH_FAILURE.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneResultFailureMapNoOp() {
    SETTABLE_DONE_WITH_RESULT.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  @Group("Done_Map")
  public void doneResultFailureMapExecutedNoOp() {
    SETTABLE_DONE_WITH_RESULT.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                         SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  @Group("Listener")
  public void setResultWithListener() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.listener(DoNothingRunnable.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("Listener")
  public void setResultWithExecutedListener() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.listener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void setResultWithResultCallback() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK);
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void setResultWithExecutedResultCallback() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("FailureCallback")
  public void setResultWithFailureCallback() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.failureCallback(FAILURE_CALLBACK);
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void setFailureWithResultCallback() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void setFailureWithExecutedResultCallback() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  @Group("FailureCallback")
  public void setFailureWithFailureCallback() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    slf.failureCallback(FAILURE_CALLBACK);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  @Group("Map_Result")
  public void setResultMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER);
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("Map_Result")
  public void setResultExecutedMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("Map_Result")
  public void setResultFailureMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER);
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("Map_Result")
  public void setResultExecutedFailureMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    slf.setResult(null);
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void setFailureMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void setFailureExecutedMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void setFailureFailureMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER);
    slf.setFailure(FAILURE);
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void setFailureExecutedFailureMappedFuture() {
    SettableListenableFuture<Void> slf = new SettableListenableFuture<>();
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = slf.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    slf.setFailure(FAILURE);
  }
}
