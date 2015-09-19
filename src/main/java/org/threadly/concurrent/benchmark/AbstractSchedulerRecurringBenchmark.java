package org.threadly.concurrent.benchmark;

public abstract class AbstractSchedulerRecurringBenchmark extends AbstractSchedulerBenchmark {
  private static final int SCHEDULE_DELAY = 5;
  
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
        
        doThreadWork(startTime);
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
