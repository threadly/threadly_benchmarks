package org.threadly.concurrent.benchmark;

import org.threadly.concurrent.AbstractSubmitterScheduler;
import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.concurrent.UnfairExecutor;

public class UnfairExecutorExecuteBenchmark extends AbstractSchedulerExecuteBenchmark {
  public static void main(String args[]) {
    try {
      new UnfairExecutorExecuteBenchmark(Integer.parseInt(args[0]), 
                                         Integer.parseInt(args[1])).runTest();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  protected final UnfairExecutor originalExecutor;
  protected final SubmitterScheduler executor;
  
  public UnfairExecutorExecuteBenchmark(int threadRunTime, int poolSize) {
    super(threadRunTime);
    
    originalExecutor = new UnfairExecutor(poolSize);
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
