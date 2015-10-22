package org.threadly.concurrent.benchmark;

import org.threadly.util.Clock;

public abstract class AbstractSchedulerExecuteBenchmark extends AbstractSchedulerBenchmark {
  protected AbstractSchedulerExecuteBenchmark(int threadRunTime) {
    super(threadRunTime);
  }

  @Override
  protected Runnable makeRunnable(int id) {
    return new TestRunnable(id);
  }
  
  private class TestRunnable implements Runnable {
    private final int index;
    
    private TestRunnable(int index) {
      this.index = index;
    }

    @Override
    public void run() {
      if (run) {
        long startTime = threadRunTime > 0 ? Clock.accurateTimeMillis() : -1;
        countArray.incrementAndGet(index);

        doThreadWork(startTime);
        
        getScheduler().execute(this);
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
