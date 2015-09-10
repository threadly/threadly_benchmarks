package org.threadly.concurrent.benchmark;

import java.util.concurrent.atomic.AtomicReferenceArray;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.KeyDistributedScheduler;

/**
 * This test is good at creating very large queues for the distributor key.
 */
public class KeyDistributedExecutorManySubmitterBenchmark extends AbstractBenchmark {
  private static final int SUBMITTER_QTY = 50;
  private static final int SCHEDULE_DELAY = 1;
  private static final Object DISTRIBUTOR_KEY = "foo";

  private static final PriorityScheduler EXECUTOR;
  private static final KeyDistributedScheduler DISTRIBUTOR;
  
  static {
    EXECUTOR = new PriorityScheduler(SUBMITTER_QTY * 2);
    EXECUTOR.prestartAllThreads();
    DISTRIBUTOR = new KeyDistributedScheduler(EXECUTOR);
  }

  private static volatile boolean run = true;
  private static volatile long execCount = 0;
  private static final AtomicReferenceArray<DistributorRunnable> lastRunnable = new AtomicReferenceArray<DistributorRunnable>(SUBMITTER_QTY);
  
  private static void spin(int maxTimeInNanos) {
    long startTime = System.nanoTime();
    int waitTime = RANDOM.nextInt(maxTimeInNanos);
    while (run && System.nanoTime() < startTime + waitTime) {
      // spin
      Thread.yield();
    }
  }
  
  public static void main(String[] args) {
    run(Boolean.parseBoolean(args[0]));
  }
  
  private static void run(final boolean schedule) {
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < SUBMITTER_QTY; i++) {
      final int index = i;
      EXECUTOR.schedule(new Runnable() {
        @Override
        public void run() {
          DistributorRunnable dr = new DistributorRunnable();
          while (run) {
            if (schedule) {
              DISTRIBUTOR.schedule(DISTRIBUTOR_KEY, dr, SCHEDULE_DELAY);
            } else {
              DISTRIBUTOR.execute(DISTRIBUTOR_KEY, dr);
            }
            
            //spin(100);
          }

          dr = new DistributorRunnable();
          if (schedule) {
            DISTRIBUTOR.schedule(DISTRIBUTOR_KEY, dr, SCHEDULE_DELAY);
          } else {
            DISTRIBUTOR.execute(DISTRIBUTOR_KEY, dr);
          }
          
          lastRunnable.set(index, dr);
        }
      }, startTime - System.currentTimeMillis() + 100);
    }

    while (System.currentTimeMillis() - startTime < RUN_TIME) {
      spin(500000); // spin for 1/2 millisecond
    }

    run = false;
    for (int i = 0; i < SUBMITTER_QTY; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (indexRunnable == null) {
        spin(500000); // spin for 1/2 millisecond
        indexRunnable = lastRunnable.get(i);
      }
    }
    long countAtStop = execCount;
    for (int i = 0; i < SUBMITTER_QTY; i++) {
      DistributorRunnable indexRunnable = lastRunnable.get(i);
      while (! indexRunnable.runFinshed) {
        spin(500000); // spin for 1/2 millisecond
      }
    }
    /*System.out.println((schedule ? "Schedule total: " : "Total: ") + 
                         execCount + " occured after stop: " + (execCount - countAtStop));*/
    System.out.println(KeyDistributedExecutorManySubmitterBenchmark.class.getSimpleName() + 
                         ": " + execCount);
  }
  
  private static class DistributorRunnable implements Runnable {
    public volatile boolean runFinshed = false;
    
    @Override
    public void run() {
      execCount++;
      
      runFinshed = true;
      
      //spin(10);
    }
  }
}
