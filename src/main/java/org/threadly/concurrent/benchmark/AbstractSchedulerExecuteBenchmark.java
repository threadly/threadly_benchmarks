package org.threadly.concurrent.benchmark;

public abstract class AbstractSchedulerExecuteBenchmark extends AbstractSchedulerBenchmark {
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
        long startTime = System.currentTimeMillis();
        countArray.incrementAndGet(index);

        while (System.currentTimeMillis() - startTime < THREAD_RUN_TIME) {
          // spin loop
        }
        getScheduler().execute(this);
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
