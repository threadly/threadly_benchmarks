package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.KeyDistributedScheduler;

/**
 * Does not need to create and destroy workers, 
 * queues remain small, and lock contention is minimal.
 */
public class KeyDistributedExecutorSimpleBenchmark extends AbstractBenchmark {
  private static final int SCHEDULE_DELAY = 1;
  private static final int POOL_SIZE = 4;
  private static final PriorityScheduler EXECUTOR;
  private static final KeyDistributedScheduler DISTRIBUTOR;
  
  static {
    EXECUTOR = new PriorityScheduler(POOL_SIZE, POOL_SIZE, 1000 * 10);
    EXECUTOR.prestartAllCoreThreads();
    DISTRIBUTOR = new KeyDistributedScheduler(EXECUTOR);
  }
  
  private static volatile boolean run = true;
  private static volatile long thread1Count = -1;
  private static volatile long thread2Count = -1;
  
  public static void main(String args[]) {
    run(Boolean.parseBoolean(args[0]), Integer.parseInt(args[1]));
  }
  
  private static void run(final boolean schedule, final int queueSize) {
    Runnable thread1 = new Runner() {
      @Override
      public void run() {
        if (run) {
          thread1Count++;
          addSelf(schedule, queueSize);
        }
      }
    };
    Runnable thread2 = new Runner() {
      @Override
      public void run() {
        if (run) {
          thread2Count++;
          addSelf(schedule, queueSize);
        }
      }
    };
    long startTime = System.currentTimeMillis();
    EXECUTOR.execute(thread1);
    EXECUTOR.execute(thread2);
    
    while (System.currentTimeMillis() - startTime < RUN_TIME) {
      // spin
      Thread.yield();
    }
    
    run = false;
    EXECUTOR.shutdownNow();
    
    System.out.println((thread1Count + thread2Count) + " = " + 
                         thread1Count + " + " + thread2Count);
  }
  
  private static abstract class Runner implements Runnable {
    private boolean firstAdd = true;
    
    protected void addSelf(boolean schedule, int queueSize) {
      if (firstAdd) {
        for (int i = 1; i < queueSize; i++) {
          DISTRIBUTOR.addTask(this, this);
        }
        firstAdd = false;
      }
      
      if (schedule) {
        DISTRIBUTOR.scheduleTask(this, this, SCHEDULE_DELAY);
      } else {
        DISTRIBUTOR.addTask(this, this);
      }
    }
  }
}