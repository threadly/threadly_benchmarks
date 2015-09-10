package org.threadly.concurrent.benchmark;

import java.util.concurrent.atomic.AtomicInteger;

import org.threadly.concurrent.NoThreadScheduler;
import org.threadly.test.concurrent.TestCondition;

public class NoThreadSchedulerBenchmark extends AbstractBenchmark {
  private static final int SUBMITTION_THREAD_COUNT = 64;
  
  public static void main(String args[]) {
    try {
      new NoThreadSchedulerBenchmark().runTest();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      System.exit(0);
    }
  }
  
  private final NoThreadScheduler scheduler = new NoThreadScheduler();
  private final AtomicInteger execCount = new AtomicInteger(0);
  private volatile boolean run = true;
  private volatile boolean tickDone = false;
  
  private void runTest() throws InterruptedException {
    run = false;
    
    for (int i = 0; i < SUBMITTION_THREAD_COUNT; i++) {
      new Thread(new Runnable() {
        private final Runnable runnable = new Runnable() {
          @Override
          public void run() {
            // no-op, run fast
          }
        };
        
        @Override
        public void run() {
          while (! run) {
            Thread.yield();
          }
          while (run) {
            scheduler.execute(runnable);
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
          runCount = scheduler.tick(null);
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
    scheduler.cancelTick();
    
    new TestCondition() {
      @Override
      public boolean get() {
        return tickDone;
      }
    }.blockTillTrue(RUN_TIME * 15, 1000);
    
    System.out.println(this.getClass().getSimpleName() + ": " + execCount.get());
  }
}
