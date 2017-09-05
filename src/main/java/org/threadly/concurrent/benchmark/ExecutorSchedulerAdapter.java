package org.threadly.concurrent.benchmark;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.threadly.concurrent.AbstractSubmitterScheduler;
import org.threadly.util.Clock;
import org.threadly.util.ExceptionUtils;

public class ExecutorSchedulerAdapter extends AbstractSubmitterScheduler {
  private final Executor executor;
  private volatile long lastScheduledTime = 0;
  private volatile Pair<Long, Queue<Runnable>> scheduledTaskQueue;
  
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
      // Create a new thread / task queue if our clock has advanced, requested delay has changed, 
      // or we have queued a lot for the previous thread (we want to start these tasks as fast as possible)
      Pair<Long, Queue<Runnable>> scheduledTaskQueue = this.scheduledTaskQueue;
      if (Clock.lastKnownForwardProgressingMillis() != lastScheduledTime || 
          scheduledTaskQueue == null || scheduledTaskQueue.getLeft() != delayInMillis || 
          scheduledTaskQueue.getRight().size() >= 100) {
        if (delayInMillis > 100) {
          final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
          taskQueue.add(task);
          this.scheduledTaskQueue = new Pair<>(delayInMillis, taskQueue);
          lastScheduledTime = Clock.lastKnownForwardProgressingMillis();
  
          new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                Thread.sleep(delayInMillis);
              } catch (InterruptedException e) {
                e.printStackTrace();
                return;
              }
              ExecutorSchedulerAdapter.this.scheduledTaskQueue = null;
              
              Runnable task;
              while ((task = taskQueue.poll()) != null) {
                ExceptionUtils.runRunnable(task);
              }
            }
          }).start();
        } else {
          // getting pretty close to when we need to run the task, safer to spin loop
          if (this.scheduledTaskQueue != null) {
            this.scheduledTaskQueue = null;
            // Considering this is only used in benchmarks, this should be an unlikely condition
            System.out.println("WARNING: Short delay task(s)!");
          }
          final long startTime = Clock.accurateForwardProgressingMillis();
          new Thread(new Runnable() {
            @Override
            public void run() {
              while (Clock.accurateForwardProgressingMillis() - startTime < delayInMillis) {
                Thread.yield();
              }
              
              ExceptionUtils.runRunnable(task);
            }
          }).start();
        }
      } else {
        scheduledTaskQueue.getRight().add(task);
      }
    } else {
      executor.execute(task);
    }
  }
}
