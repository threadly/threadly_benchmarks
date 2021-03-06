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
  public void reusable1_called() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void reusable1_executed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void reusable2_called() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void reusable2_executed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void reusable2_mixed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void reusable4_called() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void reusable4_mixed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(false);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void singleUse1_called() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void singleUse1_executed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void singleUse2_called() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void singleUse2_executed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void singleUse2_mixed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void singleUse4_called() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.callListeners();
  }
  
  @Benchmark
  public void singleUse4_mixed() {
    RunnableListenerHelper rlh = new RunnableListenerHelper(true);
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.addListener(DoNothingRunnable.instance(), SameThreadSubmitterExecutor.instance());
    rlh.callListeners();
  }
}
