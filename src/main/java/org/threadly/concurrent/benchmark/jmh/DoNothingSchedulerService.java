package org.threadly.concurrent.benchmark.jmh;

import java.util.concurrent.Callable;

import org.threadly.concurrent.AbstractSubmitterScheduler;
import org.threadly.concurrent.SchedulerService;

public class DoNothingSchedulerService extends AbstractSubmitterScheduler 
                                       implements SchedulerService {
  public static final DoNothingSchedulerService INSTANCE = new DoNothingSchedulerService();
  
  private DoNothingSchedulerService() {
    // nothing to construct
  }

  @Override
  public void scheduleWithFixedDelay(Runnable task, long initialDelay, long recurringDelay) {
    // ignored
  }

  @Override
  public void scheduleAtFixedRate(Runnable task, long initialDelay, long period) {
    // ignored
  }

  @Override
  protected void doSchedule(Runnable task, long delayInMillis) {
    // ignored
  }

  @Override
  public boolean remove(Runnable task) {
    return false;
  }

  @Override
  public boolean remove(Callable<?> task) {
    return false;
  }

  @Override
  public int getActiveTaskCount() {
    return 0;
  }

  @Override
  public int getQueuedTaskCount() {
    return 0;
  }

  @Override
  public int getWaitingForExecutionTaskCount() {
    return 0;
  }

  @Override
  public boolean isShutdown() {
    return false;
  }
}
