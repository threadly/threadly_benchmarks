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
import org.threadly.util.AbstractService;

@Fork(MicroBenchmarkRunner.FORKS)
@Warmup(iterations = MicroBenchmarkRunner.WARMUP_ITERATIONS, 
        time = MicroBenchmarkRunner.WARMUP_SECONDS, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = MicroBenchmarkRunner.RUN_ITERATIONS, 
             time = MicroBenchmarkRunner.RUN_SECONDS, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class AbstractServiceMicro {
  private static final TestAbstractService UNSTARTED_SERVICE = new TestAbstractService();
  private static final TestAbstractService STARTED_SERVICE;
  
  static {
    STARTED_SERVICE = new TestAbstractService();
    STARTED_SERVICE.start();
  }

  @Benchmark
  @Group("Ignored")
  public void noOpStopIfRunning() {
    UNSTARTED_SERVICE.stopIfRunning();
  }

  @Benchmark
  @Group("Ignored")
  public void noOpStartIfNotStarted() {
    STARTED_SERVICE.startIfNotStarted();
  }

  @Benchmark
  @Group("Invoked")
  public void start() {
    new TestAbstractService().start();
  }

  @Benchmark
  @Group("Invoked")
  public void startIfNotStarted() {
    new TestAbstractService().startIfNotStarted();
  }
  
  private static class TestAbstractService extends AbstractService {
    @Override
    protected void startupService() {
      // ignored
    }

    @Override
    protected void shutdownService() {
      // ignored
    }
    
    @Override
    protected void finalize() throws Throwable {
      // don't let super logic execute
    }
  }
}
