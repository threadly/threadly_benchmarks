package org.threadly.concurrent.benchmark;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.future.FutureUtils;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.concurrent.wrapper.KeyDistributedScheduler;
import org.threadly.util.Clock;

/**
 * Lots and lots of short lived distributors with queues the size of one.  Requires a lot of 
 * construction and GC as each worker is constructed and then tossed away. 
 */
public class KeyDistributedExecutorUniqueKeyBenchmark extends AbstractBenchmark {
  private static final int SCHEDULE_DELAY = 1;
  private static final boolean COUNT_AT_STOP = true;
  
  public static void main(String[] args) throws InterruptedException {
    new KeyDistributedExecutorUniqueKeyBenchmark(Integer.parseInt(args[1]))
            .run(Boolean.parseBoolean(args[0]));
    System.exit(0);
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
  
  protected void run(final boolean schedule) throws InterruptedException {
    long startTime = Clock.accurateForwardProgressingMillis();
    for (int i = 0; i < submitterQty; i++) {
      final int index = i;
      scheduler.schedule(new Runnable() {
        @Override
        public void run() {
          DistributorRunnable dr = new DistributorRunnable();
          ListenableFuture<?> fcFuture1 = FutureUtils.immediateResultFuture(null);
          ListenableFuture<?> fcFuture2 = FutureUtils.immediateResultFuture(null);
          while (run) {
            for (int i = 0; i < 1000 && run; i++) {
              if (schedule) {
                distributor.schedule(UniqueObject.INSTANCE, dr, SCHEDULE_DELAY);
              } else {
                distributor.execute(UniqueObject.INSTANCE, dr);
              }
            }

            while (run) {
              try {
                fcFuture1.get(200, TimeUnit.MILLISECONDS);  // block till done so we don't submit too much
                fcFuture1 = fcFuture2;
                if (run) {
                  if (schedule) {
                    fcFuture2 = distributor.submitScheduled(UniqueObject.INSTANCE, dr, SCHEDULE_DELAY);
                  } else {
                    fcFuture2 = distributor.submit(UniqueObject.INSTANCE, dr);
                  }
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
          if (schedule) {
            distributor.schedule(UniqueObject.INSTANCE, dr, SCHEDULE_DELAY);
          } else {
            distributor.execute(UniqueObject.INSTANCE, dr);
          }
          
          lastRunnable.set(index, dr);
        }
      }, startTime - Clock.accurateForwardProgressingMillis() + 100);
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
    System.out.println((schedule ? "Schedule total: " : "Total: ") + 
                         execCount.get() + " occurred after stop: " + (execCount.get() - countAtStop));
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
