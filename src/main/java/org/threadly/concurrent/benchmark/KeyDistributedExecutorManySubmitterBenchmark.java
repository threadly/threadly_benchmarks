package org.threadly.concurrent.benchmark;

import java.util.concurrent.atomic.AtomicReferenceArray;

import org.threadly.concurrent.KeyDistributedScheduler;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.util.Clock;

/**
 * This benchmark is good at creating very large queues for the distributor key.
 */
public class KeyDistributedExecutorManySubmitterBenchmark extends AbstractBenchmark {
  private static final int SCHEDULE_DELAY = 1;
  private static final Object DISTRIBUTOR_KEY = "foo";
  
  public static void main(String[] args) throws InterruptedException {
    new KeyDistributedExecutorManySubmitterBenchmark(Integer.parseInt(args[1]))
            .run(Boolean.parseBoolean(args[0]));
  }

  private final PriorityScheduler executor;
  private final KeyDistributedScheduler distributor;
  private final int submitterQty;
  private volatile boolean run = true;
  private volatile long execCount = 0;
  private final AtomicReferenceArray<DistributorRunnable> lastRunnable;
  
  public KeyDistributedExecutorManySubmitterBenchmark(int submitterQty) {
    executor = new PriorityScheduler(submitterQty * 2, submitterQty * 2, 10_000);
    executor.prestartAllCoreThreads();
    distributor = new KeyDistributedScheduler(executor);
    this.submitterQty = submitterQty;
    
    lastRunnable = 
        new AtomicReferenceArray<DistributorRunnable>(submitterQty);
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
    long startTime = Clock.accurateTimeMillis();
    for (int i = 0; i < submitterQty; i++) {
      final int index = i;
      executor.schedule(new Runnable() {
        @Override
        public void run() {
          DistributorRunnable dr = new DistributorRunnable();
          while (run) {
            if (schedule) {
              distributor.scheduleTask(DISTRIBUTOR_KEY, dr, SCHEDULE_DELAY);
            } else {
              distributor.addTask(DISTRIBUTOR_KEY, dr);
            }
            
            //spin(100);
          }

          dr = new DistributorRunnable();
          if (schedule) {
            distributor.scheduleTask(DISTRIBUTOR_KEY, dr, SCHEDULE_DELAY);
          } else {
            distributor.addTask(DISTRIBUTOR_KEY, dr);
          }
          
          lastRunnable.set(index, dr);
        }
      }, startTime - Clock.accurateTimeMillis() + 100);
    }

    Thread.sleep(RUN_TIME);

    run = false;
    for (int i = 0; i < submitterQty; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (indexRunnable == null) {
        spin(500000); // spin for 1/2 millisecond
        indexRunnable = lastRunnable.get(i);
      }
    }
    @SuppressWarnings("unused")
    long countAtStop = execCount;
    for (int i = 0; i < submitterQty; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (! indexRunnable.runFinshed) {
        spin(500000); // spin for 1/2 millisecond
      }
    }
    /*System.out.println((schedule ? "Schedule total: " : "Total: ") + 
                         execCount + " occurred after stop: " + (execCount - countAtStop));*/
    System.out.println(KeyDistributedExecutorManySubmitterBenchmark.class.getSimpleName() + 
                         OUTPUT_DELIM + execCount);
  }
  
  private class DistributorRunnable implements Runnable {
    public volatile boolean runFinshed = false;
    
    @Override
    public void run() {
      execCount++;
      
      runFinshed = true;
      
      //spin(10);
    }
  }
}
