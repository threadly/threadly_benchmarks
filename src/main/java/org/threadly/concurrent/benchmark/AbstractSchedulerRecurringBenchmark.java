package org.threadly.concurrent.benchmark;

import org.threadly.util.Clock;

public abstract class AbstractSchedulerRecurringBenchmark extends AbstractSchedulerBenchmark {
  private final int scheduleDelay;
  
  protected AbstractSchedulerRecurringBenchmark(int threadRunTime) {
    super(threadRunTime);
    if (threadRunTime < 1) {
      scheduleDelay = 1;
    } else if (threadRunTime < 2) {
      scheduleDelay = 2;
    } else {
      scheduleDelay = 5;
    }
  }

  @Override
  protected Runnable makeRunnable(int id) {
    return new TestRunnable(id);
  }
  
  @Override
  protected void initialSchedule(Runnable r, long delayInMs) {
    getScheduler().scheduleWithFixedDelay(r, delayInMs, scheduleDelay);
  }
  
  private class TestRunnable implements Runnable {
    private final int index;
    
    private TestRunnable(int index) {
      this.index = index;
    }

    @Override
    public void run() {
      if (run) {
        long startTime = threadRunTime > 0 ? Clock.accurateForwardProgressingMillis() : -1;
        countArray.incrementAndGet(index);
        
        doThreadWork(startTime);
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
