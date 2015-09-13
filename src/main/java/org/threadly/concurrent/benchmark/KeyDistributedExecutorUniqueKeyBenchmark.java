package org.threadly.concurrent.benchmark;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.KeyDistributedScheduler;

/**
 * Lots and lots of short lived distributors with queues the size of one.  Requires a lot of 
 * construction and GC as each worker is constructed and then tossed away. 
 */
public class KeyDistributedExecutorUniqueKeyBenchmark extends AbstractBenchmark {
  private static final int SCHEDULE_DELAY = 1;
  
  public static void main(String[] args) {
    new KeyDistributedExecutorUniqueKeyBenchmark(Integer.parseInt(args[1]))
            .run(Boolean.parseBoolean(args[0]));
  }
  
  private final int submitterQty;
  private final PriorityScheduler scheduler;
  private final KeyDistributedScheduler distributor;
  private final AtomicLong execCount = new AtomicLong(0);
  private final AtomicReferenceArray<DistributorRunnable> lastRunnable;
  private volatile boolean run = true;
  
  public KeyDistributedExecutorUniqueKeyBenchmark(int submitterQty) {
    this.submitterQty = submitterQty;
    scheduler = new PriorityScheduler(submitterQty * 2);
    scheduler.prestartAllThreads();
    distributor = new KeyDistributedScheduler(scheduler);
    lastRunnable = new AtomicReferenceArray<DistributorRunnable>(submitterQty);
  }
  
  private void spin(int maxTimeInNanos) {
    long startTime = System.nanoTime();
    int waitTime = RANDOM.nextInt(maxTimeInNanos);
    while (run && System.nanoTime() < startTime + waitTime) {
      // spin
      Thread.yield();
    }
  }
  
  protected void run(final boolean schedule) {
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < submitterQty; i++) {
      final int index = i;
      scheduler.schedule(new Runnable() {
        @Override
        public void run() {
          DistributorRunnable dr = new DistributorRunnable();
          while (run) {
            if (schedule) {
              distributor.schedule(new Object(), dr, SCHEDULE_DELAY);
            } else {
              distributor.execute(new Object(), dr);
            }
            
            //spin(100);
          }

          dr = new DistributorRunnable();
          if (schedule) {
            distributor.schedule(new Object(), dr, SCHEDULE_DELAY);
          } else {
            distributor.execute(new Object(), dr);
          }
          
          lastRunnable.set(index, dr);
        }
      }, startTime - System.currentTimeMillis() + 100);
    }

    while (System.currentTimeMillis() - startTime < RUN_TIME) {
      spin(500000); // spin for 1/2 millisecond
    }

    run = false;
    for (int i = 0; i < submitterQty; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (indexRunnable == null) {
        spin(500000); // spin for 1/2 millisecond
        indexRunnable = lastRunnable.get(i);
      }
    }
    @SuppressWarnings("unused")
    long countAtStop = execCount.get();
    for (int i = 0; i < submitterQty; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (! indexRunnable.runFinshed) {
        spin(500000); // spin for 1/2 millisecond
      }
    }
    /*System.out.println((schedule ? "Schedule total: " : "Total: ") + 
                         execCount.get() + " occurred after stop: " + (execCount.get() - countAtStop));*/
    System.out.println(KeyDistributedExecutorUniqueKeyBenchmark.class.getSimpleName() + 
                         OUTPUT_DELIM + execCount.get());
  }
  
  private class DistributorRunnable implements Runnable {
    public volatile boolean runFinshed = false;
    
    @Override
    public void run() {
      execCount.incrementAndGet();
      
      runFinshed = true;
      
      //spin(10);
    }
  }
}
