package org.threadly.concurrent.benchmark;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.threadly.concurrent.UnfairExecutor;
import org.threadly.concurrent.future.FutureUtils;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.concurrent.wrapper.KeyDistributedExecutor;
import org.threadly.util.Clock;

/**
 * Lots and lots of short lived distributors with queues the size of one.  Requires a lot of 
 * construction and GC as each worker is constructed and then tossed away. 
 */
public class KeyDistributedExecutorUniqueKeyUnfairExecutorBenchmark extends AbstractBenchmark {
  private static final boolean COUNT_AT_STOP = true;
  
  public static void main(String[] args) throws InterruptedException {
    new KeyDistributedExecutorUniqueKeyUnfairExecutorBenchmark(Integer.parseInt(args[0])).run();
    System.exit(0);
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
  
  protected void run() throws InterruptedException {
    final long startTime = Clock.accurateForwardProgressingMillis();
    for (int i = 0; i < submitterQty; i++) {
      final int index = i;
      new Thread() {
        @Override
        public void run() {
          try {
            Thread.sleep(startTime - Clock.accurateForwardProgressingMillis() + 100);
          } catch (InterruptedException e) {
            e.printStackTrace();
            return;
          }
          
          DistributorRunnable dr = new DistributorRunnable();
          ListenableFuture<?> fcFuture1 = FutureUtils.immediateResultFuture(null);
          ListenableFuture<?> fcFuture2 = fcFuture1;
          while (run) {
            for (int i = 0; i < 1000 && run; i++) {
              distributor.execute(UniqueObject.INSTANCE, dr);
            }

            while (run) {
              try {
                fcFuture1.get(200, TimeUnit.MILLISECONDS);  // block till done so we don't submit too much
                fcFuture1 = fcFuture2;
                if (run) {
                  fcFuture2 = distributor.submit(UniqueObject.INSTANCE, dr);
                }
                break;
              } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
              } catch (TimeoutException e) {
                // retry if still running
              }
            }
          }

          dr = new DistributorRunnable();
          distributor.execute(UniqueObject.INSTANCE, dr);
          
          lastRunnable.set(index, dr);
        }
      }.start();
    }

    Thread.sleep(RUN_TIME);

    run = false;
    long countAtStop = execCount.get();
    if (COUNT_AT_STOP) {
      System.out.println(KeyDistributedExecutorUniqueKeyBenchmark.class.getSimpleName() + 
                           OUTPUT_DELIM + countAtStop);
      return;
    }
    for (int i = 0; i < submitterQty; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (indexRunnable == null) {
        spin(500000); // spin for 1/2 millisecond
        indexRunnable = lastRunnable.get(i);
      }
    }
    for (int i = 0; i < submitterQty; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (! indexRunnable.runFinshed) {
        spin(500000); // spin for 1/2 millisecond
      }
    }
    System.out.println("Total: " + execCount.get() + 
                         " occurred after stop: " + (execCount.get() - countAtStop));
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
  
  private static class UniqueObject {
    public static final UniqueObject INSTANCE = new UniqueObject();
    
    @Override
    public int hashCode() {
      return ThreadLocalRandom.current().nextInt();
    }
    
    @Override
    public boolean equals(Object o) {
      return false;
    }
  }
}
