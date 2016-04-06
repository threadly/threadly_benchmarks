package org.threadly.concurrent.benchmark;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.threadly.concurrent.UnfairExecutor;
import org.threadly.concurrent.wrapper.KeyDistributedExecutor;

/**
 * Lots and lots of short lived distributors with queues the size of one.  Requires a lot of 
 * construction and GC as each worker is constructed and then tossed away. 
 */
public class KeyDistributedExecutorUniqueKeyUnfairExecutorBenchmark extends AbstractBenchmark {
  public static void main(String[] args) {
    new KeyDistributedExecutorUniqueKeyUnfairExecutorBenchmark(Integer.parseInt(args[0])).run();
  }
  
  private final int submitterQty;
  private final UnfairExecutor scheduler;
  private final KeyDistributedExecutor distributor;
  private final AtomicLong execCount = new AtomicLong(0);
  private final AtomicReferenceArray<DistributorRunnable> lastRunnable;
  private volatile boolean run = true;
  
  public KeyDistributedExecutorUniqueKeyUnfairExecutorBenchmark(int submitterQty) {
    this.submitterQty = submitterQty;
    scheduler = new UnfairExecutor((submitterQty * 2) + 1);
    distributor = new KeyDistributedExecutor(scheduler);
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
  
  protected void run() {
    final long startTime = System.currentTimeMillis();
    for (int i = 0; i < submitterQty; i++) {
      final int index = i;
      new Thread() {
        @Override
        public void run() {
          try {
            Thread.sleep(startTime - System.currentTimeMillis() + 100);
          } catch (InterruptedException e) {
            e.printStackTrace();
            return;
          }
          
          DistributorRunnable dr = new DistributorRunnable();
          while (run) {
            distributor.execute(new Object(), dr);
            
            //spin(100);
          }

          dr = new DistributorRunnable();
          distributor.execute(new Object(), dr);
          
          lastRunnable.set(index, dr);
        }
      }.start();
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
    System.out.println(KeyDistributedExecutorUniqueKeyUnfairExecutorBenchmark.class.getSimpleName() + 
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
