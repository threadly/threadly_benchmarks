package org.threadly.concurrent.benchmark;

import org.threadly.util.Clock;

public abstract class AbstractSchedulerScheduleBenchmark extends AbstractSchedulerBenchmark {
  private static final boolean DIFFER_SCHEDULE_TIME = false;
  
  private final int scheduleDelay;
  
  protected AbstractSchedulerScheduleBenchmark(int threadRunTime) {
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
  
  private class TestRunnable implements Runnable {
    private final int index;
    
    private TestRunnable(int index) {
      this.index = index;
    }

    @Override
    public void run() {
      long startTime = threadRunTime > 0 ? Clock.accurateTimeMillis() : -1;
      if (run) {
        countArray.incrementAndGet(index);
        
        long scheduleDelay;
        if (DIFFER_SCHEDULE_TIME) {
          scheduleDelay = AbstractSchedulerScheduleBenchmark.this.scheduleDelay * (index / 2);
        } else {
          scheduleDelay = AbstractSchedulerScheduleBenchmark.this.scheduleDelay;
        }
        
        doThreadWork(startTime);
        
        getScheduler().schedule(this, scheduleDelay);
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
