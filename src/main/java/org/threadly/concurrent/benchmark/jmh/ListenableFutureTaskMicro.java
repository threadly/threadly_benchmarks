package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.Callable;
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
import org.threadly.concurrent.future.ListenableFutureTask;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class ListenableFutureTaskMicro extends AbstractListenableFutureMicro {
  private static final Callable<Void> THROWING_CALLABLE = () -> { throw FAILURE; };
  private static final ListenableFutureTask<Void> TASK_DONE_WITH_RESULT;
  private static final ListenableFutureTask<Void> TASK_DONE_WITH_FAILURE;
  
  static {
    TASK_DONE_WITH_RESULT = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    TASK_DONE_WITH_RESULT.run();
    TASK_DONE_WITH_FAILURE = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    TASK_DONE_WITH_FAILURE.run();
  }
  
  @Benchmark
  @Group("Run")
  public void runResult() {
    new ListenableFutureTask<Void>(false, DoNothingRunnable.instance()).run();
  }
  
  @Benchmark
  @Group("Done")
  public void doneResultWithListener() {
    TASK_DONE_WITH_RESULT.listener(DoNothingRunnable.instance());
  }
  
  @Benchmark
  @Group("Done")
  public void doneResultWithResultCallback() {
    TASK_DONE_WITH_RESULT.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  @Group("Done")
  public void doneResultWithFailureCallback() {
    TASK_DONE_WITH_RESULT.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  @Group("Done")
  public void doneFailureWithResultCallback() {
    TASK_DONE_WITH_FAILURE.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  @Group("Done")
  public void doneFailureWithFailureCallback() {
    TASK_DONE_WITH_FAILURE.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  @Group("Listener")
  public void runResultWithListener() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.run();
  }
  
  @Benchmark
  @Group("Listener")
  public void runResultWithExecutedListener() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void runResultWithResultCallback() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.resultCallback(RESULT_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void runResultWithExecutedResultCallback() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  @Group("FailureCallback")
  public void runResultWithFailureCallback() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.failureCallback(FAILURE_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void runFailureWithResultCallback() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    lft.resultCallback(RESULT_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  @Group("ResultCallback")
  public void runFailureWithExecutedResultCallback() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    lft.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  @Group("FailureCallback")
  public void runFailureWithFailureCallback() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    lft.failureCallback(FAILURE_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Result")
  public void runResultMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER);
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Result")
  public void runResultExecutedMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Result")
  public void runResultFailureMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER);
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Result")
  public void runResultExecutedFailureMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void runFailureMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER);
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void runFailureExecutedMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void runFailureFailureMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER);
    lft.run();
  }
  
  @Benchmark
  @Group("Map_Failure")
  public void runFailureExecutedFailureMappedFuture() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    lft.run();
  }
}
