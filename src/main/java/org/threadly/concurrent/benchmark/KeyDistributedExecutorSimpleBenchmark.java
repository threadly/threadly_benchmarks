package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.KeyDistributedScheduler;
import org.threadly.concurrent.PriorityScheduler;

/**
 * Does not need to create and destroy workers, queues remain small, and lock contention is 
 * minimal.
 */
public class KeyDistributedExecutorSimpleBenchmark extends AbstractBenchmark {
  public static void main(String[] args) throws InterruptedException {
    run(Boolean.parseBoolean(args[0]));
  }
  
  private static final int SCHEDULE_DELAY = 1;
  private static final int POOL_SIZE = 4;
  private static final int QUEUE_SIZE = 1000;
  private static final PriorityScheduler EXECUTOR;
  private static final KeyDistributedScheduler DISTRIBUTOR;
  
  static {
    EXECUTOR = new PriorityScheduler(POOL_SIZE);
    EXECUTOR.prestartAllThreads();
    DISTRIBUTOR = new KeyDistributedScheduler(EXECUTOR);
  }
  
  private static volatile boolean run = true;
  private static volatile long thread1Count = -1;
  private static volatile long thread2Count = -1;
  
  protected static void run(final boolean schedule) throws InterruptedException {
    Runnable thread1 = new Runner() {
      @Override
      public void run() {
        if (run) {
          thread1Count++;
          addSelf(schedule);
        }
      }
    };
    Runnable thread2 = new Runner() {
      @Override
      public void run() {
        if (run) {
          thread2Count++;
          addSelf(schedule);
        }
      }
    };
    EXECUTOR.execute(thread1);
    EXECUTOR.execute(thread2);

    Thread.sleep(RUN_TIME);
    
    run = false;
    EXECUTOR.shutdownNow();
    
    /*System.out.println((thread1Count + thread2Count) + " = " + 
                         thread1Count + " + " + thread2Count);*/
    System.out.println(KeyDistributedExecutorSimpleBenchmark.class.getSimpleName() + 
                         OUTPUT_DELIM + (thread1Count + thread2Count));
  }
  
  private static abstract class Runner implements Runnable {
    private boolean firstAdd = true;
    
    protected void addSelf(boolean schedule) {
      if (firstAdd) {
        for (int i = 1; i < QUEUE_SIZE; i++) {
          DISTRIBUTOR.execute(this, this);
        }
        firstAdd = false;
      }
      
      if (schedule) {
        DISTRIBUTOR.schedule(this, this, SCHEDULE_DELAY);
      } else {
        DISTRIBUTOR.execute(this, this);
      }
    }
  }
}
