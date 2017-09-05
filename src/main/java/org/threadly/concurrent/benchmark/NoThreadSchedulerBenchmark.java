package org.threadly.concurrent.benchmark;

import java.util.concurrent.atomic.AtomicInteger;

import org.threadly.concurrent.NoThreadScheduler;

public class NoThreadSchedulerBenchmark extends AbstractBenchmark {
  public static void main(String args[]) {
    try {
      new NoThreadSchedulerBenchmark().runTest(Integer.parseInt(args[0]));
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      System.exit(0);
    }
  }
  
  private final NoThreadScheduler scheduler = new NoThreadScheduler(false);
  private final AtomicInteger execCount = new AtomicInteger(0);
  private volatile boolean run = true;
  private volatile boolean tickDone = false;
  
  private void runTest(int submitterThreadCount) throws InterruptedException {
    run = false;
    
    for (int i = 0; i < submitterThreadCount; i++) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          while (! run) {
            Thread.yield();
          }
          while (run) {
            scheduler.execute(new Runnable() {
              @Override
              public void run() {
                // do nothing
              }
            });
          }
        }
      }).start();
    }
    
    // start submitting tasks
    run = true;
    tickDone = false;
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (run) {
          int runCount;
          try {
            runCount = scheduler.tick();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // should not be possible
            throw new RuntimeException(e);
          }
          if (runCount > 0) {
            execCount.addAndGet(runCount);
          } else {
            Thread.yield();
          }
        }
        tickDone = true;
      }
    }).start();
    
    // run the test, stopping after the time expires
    Thread.sleep(RUN_TIME);
    run = false;

    while (! tickDone) {
      Thread.sleep(100);
    }
    
    System.out.println(this.getClass().getSimpleName() + ": " + execCount.get());
  }
}
