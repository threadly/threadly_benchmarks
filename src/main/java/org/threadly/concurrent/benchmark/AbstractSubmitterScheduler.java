package org.threadly.concurrent.benchmark;

import java.util.concurrent.Callable;

import org.threadly.concurrent.SubmitterSchedulerInterface;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.concurrent.future.ListenableFutureTask;

public abstract class AbstractSubmitterScheduler extends AbstractSubmitterExecutor
                                                 implements SubmitterSchedulerInterface {
  @Override
  protected void doExecute(Runnable task) {
    doSchedule(task, 0);
  }

  /**
   * Should schedule the provided task.  All error checking has completed by this point.
   * 
   * @param task Runnable ready to be ran
   * @param delayInMillis delay to schedule task out to
   */
  protected abstract void doSchedule(Runnable task, long delayInMillis);
  
  @Override
  public void schedule(Runnable task, long delayInMs) {
    if (task == null) {
      throw new IllegalArgumentException("Task can not be null");
    } else if (delayInMs < 0) {
      throw new IllegalArgumentException("delayInMs can not be negative");
    }
    
    doSchedule(task, delayInMs);
  }

  @Override
  public ListenableFuture<?> submitScheduled(Runnable task, long delayInMs) {
    return submitScheduled(task, null, delayInMs);
  }

  @Override
  public <T> ListenableFuture<T> submitScheduled(Runnable task, T result, long delayInMs) {
    if (task == null) {
      throw new IllegalArgumentException("Task can not be null");
    } else if (delayInMs < 0) {
      throw new IllegalArgumentException("delayInMs can not be negative");
    }
    
    ListenableFutureTask<T> lft = new ListenableFutureTask<T>(false, task, result);

    doSchedule(lft, delayInMs);
    
    return lft;
  }

  @Override
  public <T> ListenableFuture<T> submitScheduled(Callable<T> task, long delayInMs) {
    if (task == null) {
      throw new IllegalArgumentException("Task can not be null");
    } else if (delayInMs < 0) {
      throw new IllegalArgumentException("delayInMs can not be negative");
    }
    
    ListenableFutureTask<T> lft = new ListenableFutureTask<T>(false, task);

    doSchedule(lft, delayInMs);
    
    return lft;
  }
}
