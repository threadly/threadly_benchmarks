package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.threadly.concurrent.ReschedulingOperation;
import org.threadly.concurrent.SameThreadSubmitterExecutor;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class ReschedulingOperationMicro {
  private static final ReschedulingOperation FAST_OPERATION = 
      new ReschedulingOperation(SameThreadSubmitterExecutor.instance()) {
        @Override
        protected void run() {
          // ignored
        }
      };
  private static final ReschedulingOperation SIGNAL_RUN_OPERATION = 
      new ReschedulingOperation(SameThreadSubmitterExecutor.instance()) {
        private boolean flip = true;
        
        @Override
        protected void run() {
          if (flip) {
            flip = false;
            signalToRun();
          } else {
            flip = true;
          }
        }
      };

  @Benchmark
  public void synchronous_signalToRun() {
    FAST_OPERATION.signalToRun();
    FAST_OPERATION.signalToRun(); // Run twice to make invoke counts match signalToRunFromRun
  }

  @Benchmark
  public void synchronous_signalToRunFromRun() {
    SIGNAL_RUN_OPERATION.signalToRun();
  }

  @Benchmark
  @GroupThreads(2)
  public void threaded_signalToRun() {
    FAST_OPERATION.signalToRun();
    FAST_OPERATION.signalToRun();
  }

  @Benchmark
  @GroupThreads(2)
  public void threaded_signalToRunFromRun() {
    SIGNAL_RUN_OPERATION.signalToRun();
  }
}
