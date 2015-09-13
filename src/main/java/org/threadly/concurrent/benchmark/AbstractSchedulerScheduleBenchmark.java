package org.threadly.concurrent.benchmark;

public abstract class AbstractSchedulerScheduleBenchmark extends AbstractSchedulerBenchmark {
  private static final int SCHEDULE_DELAY = 10;
  private static final boolean DIFFER_SCHEDULE_TIME = false;
  
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
      long startTime = System.currentTimeMillis();
      if (run) {
        countArray.incrementAndGet(index);
        
        long scheduleDelay;
        if (DIFFER_SCHEDULE_TIME) {
          scheduleDelay = SCHEDULE_DELAY * index;
        } else {
          scheduleDelay = SCHEDULE_DELAY;
        }
        
        while (System.currentTimeMillis() - startTime < THREAD_RUN_TIME) {
          // spin loop
        }
        
        getScheduler().schedule(this, scheduleDelay);
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
