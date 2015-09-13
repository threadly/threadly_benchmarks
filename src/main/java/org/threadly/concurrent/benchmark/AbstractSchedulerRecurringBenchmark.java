package org.threadly.concurrent.benchmark;

public abstract class AbstractSchedulerRecurringBenchmark extends AbstractSchedulerBenchmark {
  private static final int SCHEDULE_DELAY = 10;
  
  @Override
  protected Runnable makeRunnable(int id) {
    return new TestRunnable(id);
  }
  
  @Override
  protected void initialSchedule(Runnable r, long delayInMs) {
    getScheduler().scheduleWithFixedDelay(r, delayInMs, SCHEDULE_DELAY);
  }
  
  private class TestRunnable implements Runnable {
    private final int index;
    
    private TestRunnable(int index) {
      this.index = index;
    }

    @Override
    public void run() {
      if (run) {
      long startTime = System.currentTimeMillis();
        countArray.incrementAndGet(index);
        
        while (System.currentTimeMillis() - startTime < THREAD_RUN_TIME) {
          // spin loop
        }
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
