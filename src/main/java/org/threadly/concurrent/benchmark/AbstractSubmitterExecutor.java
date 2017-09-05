package org.threadly.concurrent.benchmark;

import java.util.concurrent.Callable;

import org.threadly.concurrent.SubmitterExecutorInterface;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.concurrent.future.ListenableFutureTask;

public abstract class AbstractSubmitterExecutor implements SubmitterExecutorInterface {
  /**
   * Should execute the provided task, or provide the task to a given 
   * executor.
   * 
   * @param task Runnable ready to be ran
   */
  protected abstract void doExecute(Runnable task);
  
  @Override
  public void execute(Runnable task) {
    if (task == null) {
      throw new IllegalArgumentException("Must provide task");
    }
    
    doExecute(task);
  }

  @Override
  public ListenableFuture<?> submit(Runnable task) {
    return submit(task, null);
  }

  @Override
  public <T> ListenableFuture<T> submit(Runnable task, T result) {
    if (task == null) {
      throw new IllegalArgumentException("Must provide task");
    }
    
    ListenableFutureTask<T> lft = new ListenableFutureTask<T>(false, task, result);
    
    doExecute(lft);
    
    return lft;
  }

  @Override
  public <T> ListenableFuture<T> submit(Callable<T> task) {
    if (task == null) {
      throw new IllegalArgumentException("Must provide task");
    }
    
    ListenableFutureTask<T> lft = new ListenableFutureTask<T>(false, task);
    
    doExecute(lft);
    
    return lft;
  }
}
