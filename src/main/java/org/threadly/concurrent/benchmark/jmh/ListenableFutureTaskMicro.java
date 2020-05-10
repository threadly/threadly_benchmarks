package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
import org.threadly.concurrent.SingleThreadScheduler;
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
  private static final SingleThreadScheduler SCHEDULER;
  
  static {
    SCHEDULER = new SingleThreadScheduler();
    SCHEDULER.prestartExecutionThread();
    
    TASK_DONE_WITH_RESULT = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    TASK_DONE_WITH_RESULT.run();
    TASK_DONE_WITH_FAILURE = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    TASK_DONE_WITH_FAILURE.run();
  }
  
  @Benchmark
  public void constructRun_direct() {
    new ListenableFutureTask<Void>(false, DoNothingRunnable.instance()).run();
  }
  
  @Benchmark
  public void constructRun_singleThreadSchedulerSubmitAndGet() throws InterruptedException, ExecutionException {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<Void>(false, DoNothingRunnable.instance());
    SCHEDULER.execute(lft);
    lft.get();
  }
  
  @Benchmark
  public void doneResult_listener() {
    TASK_DONE_WITH_RESULT.listener(DoNothingRunnable.instance());
  }
  
  @Benchmark
  public void doneResult_resultCallback() {
    TASK_DONE_WITH_RESULT.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  public void doneResult_failureCallback() {
    TASK_DONE_WITH_RESULT.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  public void doneFailure_resultCallback() {
    TASK_DONE_WITH_FAILURE.resultCallback(RESULT_CALLBACK);
  }
  
  @Benchmark
  public void doneFailure_failureCallback() {
    TASK_DONE_WITH_FAILURE.failureCallback(FAILURE_CALLBACK);
  }
  
  @Benchmark
  public void doneMapResult_mapped() {
    TASK_DONE_WITH_RESULT.map(MAPPRER);
  }
  
  @Benchmark
  public void doneMapResult_executedMapped() {
    TASK_DONE_WITH_RESULT.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void doneMapResult_failureMapNoOp() {
    TASK_DONE_WITH_RESULT.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  public void doneMapResult_failureMapExecutedNoOp() {
    TASK_DONE_WITH_RESULT.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                     SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void doneMapFailure_mapped() {
    TASK_DONE_WITH_FAILURE.mapFailure(Exception.class, FAILURE_MAPPRER);
  }
  
  @Benchmark
  public void doneMapFailure_executedMapped() {
    TASK_DONE_WITH_FAILURE.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                      SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void doneMapFailure_mapNoOp() {
    TASK_DONE_WITH_FAILURE.map(MAPPRER);
  }
  
  @Benchmark
  public void doneMapFailure_executedMapNoOp() {
    TASK_DONE_WITH_FAILURE.map(MAPPRER, SameThreadSubmitterExecutor.instance());
  }
  
  @Benchmark
  public void listener_1CalledOnRun() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.run();
  }
  
  @Benchmark
  public void listener_1ExecutedOnRun() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void listener_2CalledOnRun() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.run();
  }
  
  @Benchmark
  public void listener_4CalledOnRun() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.listener(DoNothingRunnable.instance());
    lft.run();
  }
  
  @Benchmark
  public void resultCallbackOnRun_callWithResult() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.resultCallback(RESULT_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  public void resultCallbackOnRun_executedWithResult() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void resultCallbackOnRun_failureNoOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    lft.resultCallback(RESULT_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  public void resultCallbackOnRun_executedFailureNoOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    lft.resultCallback(RESULT_CALLBACK, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void failureCallbackOnRun_callWithFailure() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    lft.failureCallback(FAILURE_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  public void failureCallbackOnRun_executedWithFailure() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    lft.failureCallback(FAILURE_CALLBACK, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void failureCallbackOnRun_noOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.failureCallback(FAILURE_CALLBACK);
    lft.run();
  }
  
  @Benchmark
  public void failureCallbackOnRun_executedNoOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    lft.failureCallback(FAILURE_CALLBACK, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void mapResultOnRun_mapped() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER);
    lft.run();
  }
  
  @Benchmark
  public void mapResultOnRun_executedMapped() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void mapResultOnRun_failureMapNoOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER);
    lft.run();
  }
  
  @Benchmark
  public void mapResultOnRun_executedFailureMapNoOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, DoNothingRunnable.instance());
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void mapFailureOnRun_mapNoOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER);
    lft.run();
  }
  
  @Benchmark
  public void mapFailureOnRun_executedMapNoOp() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.map(MAPPRER, SameThreadSubmitterExecutor.instance());
    lft.run();
  }
  
  @Benchmark
  public void mapFailureOnRun_mapped() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER);
    lft.run();
  }
  
  @Benchmark
  public void mapFailureOnRun_executedMapped() {
    ListenableFutureTask<Void> lft = new ListenableFutureTask<>(false, THROWING_CALLABLE);
    @SuppressWarnings("unused")
    ListenableFuture<Void> mapped = lft.mapFailure(Exception.class, FAILURE_MAPPRER, 
                                                   SameThreadSubmitterExecutor.instance());
    lft.run();
  }
}
