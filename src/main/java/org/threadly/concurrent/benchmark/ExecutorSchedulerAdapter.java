package org.threadly.concurrent.benchmark;

import java.util.concurrent.Executor;

import org.threadly.concurrent.AbstractSubmitterScheduler;
import org.threadly.util.ExceptionUtils;

public class ExecutorSchedulerAdapter extends AbstractSubmitterScheduler {
  private final Executor executor;
  
  public ExecutorSchedulerAdapter(Executor executor) {
    this.executor = executor;
  }

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
            e.printStackTrace();
            return;
          }
          
          ExceptionUtils.runRunnable(task);
        }
      }).start();
    } else {
      executor.execute(task);
    }
  }
}
