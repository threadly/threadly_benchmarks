package org.threadly.concurrent.benchmark;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.threadly.concurrent.SimpleSchedulerInterface;

public abstract class AbstractSubmitterScheduler implements SimpleSchedulerInterface {
  @Override
  public void execute(Runnable task) {
    doSchedule(task, 0);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return submit(task, null);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    if (task == null) {
      throw new IllegalArgumentException("Must provide task");
    }
    
    FutureTask<T> lft = new FutureTask<T>(task, result);

    doSchedule(lft, 0);
    
    return lft;
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    if (task == null) {
      throw new IllegalArgumentException("Must provide task");
    }
    
    FutureTask<T> lft = new FutureTask<T>(task);

    doSchedule(lft, 0);
    
    return lft;
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
  public Future<?> submitScheduled(Runnable task, long delayInMs) {
    return submitScheduled(task, null, delayInMs);
  }

  @Override
  public <T> Future<T> submitScheduled(Runnable task, T result, long delayInMs) {
    if (task == null) {
      throw new IllegalArgumentException("Task can not be null");
    } else if (delayInMs < 0) {
      throw new IllegalArgumentException("delayInMs can not be negative");
    }
    
    FutureTask<T> lft = new FutureTask<T>(task, result);

    doSchedule(lft, delayInMs);
    
    return lft;
  }

  @Override
  public <T> Future<T> submitScheduled(Callable<T> task, long delayInMs) {
    if (task == null) {
      throw new IllegalArgumentException("Task can not be null");
    } else if (delayInMs < 0) {
      throw new IllegalArgumentException("delayInMs can not be negative");
    }
    
    FutureTask<T> lft = new FutureTask<T>(task);

    doSchedule(lft, delayInMs);
    
    return lft;
  }
}
