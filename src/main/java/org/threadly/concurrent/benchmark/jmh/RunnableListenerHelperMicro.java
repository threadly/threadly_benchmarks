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
import org.threadly.concurrent.event.RunnableListenerHelper;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class RunnableListenerHelperMicro {
  @Benchmark
  @Group("Reusable_CallSingleListener")
  public void reusableSingleListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  @Group("Reusable_CallSingleListener")
  public void reusableSingleExecutedListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  @Benchmark
  @Group("Reusable_CallMultipleListeners")
  public void reusableDoubleListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  @Group("Reusable_CallMultipleListeners")
  public void reusableDoubleExecutedListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  @Group("Reusable_CallMultipleListeners")
  public void reusableMixedListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  @Group("SingleUse_CallSingleListener")
  public void singleUseSingleListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  @Group("SingleUse_CallSingleListener")
  public void singleUseSingleExecutedListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  @Benchmark
  @Group("SingleUse_CallMultipleListeners")
  public void singleUseDoubleListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  @Group("SingleUse_CallMultipleListeners")
  public void singleUseDoubleExecutedListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  @Group("SingleUse_CallMultipleListeners")
  public void singleUseMixedListener() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
}
