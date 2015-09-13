package org.threadly.concurrent.benchmark;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.threadly.concurrent.AbstractSubmitterScheduler;
import org.threadly.concurrent.SubmitterScheduler;

public class JavaUtilConcurrentExecutorExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new JavaUtilConcurrentExecutorExecuteBenchmark(Integer.parseInt(args[0])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final ThreadPoolExecutor originalExecutor;
  protected final SubmitterScheduler executor;
  
  public JavaUtilConcurrentExecutorExecuteBenchmark(int poolSize) {
    originalExecutor = new ThreadPoolExecutor(poolSize, poolSize, 
                                              Integer.MAX_VALUE, TimeUnit.MILLISECONDS, 
                                              new ArrayBlockingQueue<Runnable>(RUNNABLE_COUNT));
    originalExecutor.prestartAllCoreThreads();
    executor = new AbstractSubmitterScheduler() {
      @Override
      public void scheduleWithFixedDelay(Runnable task, long initialDelay, long recurringDelay) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void scheduleAtFixedRate(Runnable task, long initialDelay, long period) {
        throw new UnsupportedOperationException();
      }

      @Override
      protected void doSchedule(final Runnable task, final long delayInMillis) {
        if (delayInMillis > 0) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                Thread.sleep(delayInMillis);
              } catch (InterruptedException e) {
                // ignored
              }
              
              task.run();
            }
          }).start();
        } else {
          originalExecutor.execute(task);
        }
      }
    };
  }

  @Override
  protected SubmitterScheduler getScheduler() {
    return executor;
  }

  @Override
  protected void shutdownScheduler() {
    originalExecutor.shutdownNow();
  }
}
