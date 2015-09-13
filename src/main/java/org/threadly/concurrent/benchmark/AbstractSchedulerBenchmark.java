package org.threadly.concurrent.benchmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.util.Clock;
import org.threadly.util.debug.Profiler;

public abstract class AbstractSchedulerBenchmark extends AbstractBenchmark {
  protected static final int RUNNABLE_COUNT = 1000;
  protected static final int RUNNABLE_ADD_TIME = 1000 * 5 * 1;
  protected static final int THREAD_RUN_TIME = 20;
  protected static final boolean RUN_PROFILER = false;
  
  protected final AtomicIntegerArray countArray = new AtomicIntegerArray(RUNNABLE_COUNT);
  protected volatile boolean run = true;
  
  protected abstract SubmitterScheduler getScheduler();
  
  protected abstract void shutdownScheduler();
  
  protected abstract Runnable makeRunnable(int id);
  
  protected void initialSchedule(Runnable r, long delayInMs) {
    getScheduler().schedule(r, delayInMs);
  }
  
  protected void runTest() throws InterruptedException {
    run = true;
    
    List<Runnable> runnables = new ArrayList<Runnable>(RUNNABLE_COUNT);
    for (int i = 0; i < RUNNABLE_COUNT; i++) {
      runnables.add(makeRunnable(i));
    }
    Profiler p;
    if (RUN_PROFILER) {
      p = new Profiler(10);
      p.start();
    }

    long startTime = Clock.accurateTimeMillis();
    Iterator<Runnable> it = runnables.iterator();
    while (it.hasNext()) {
      initialSchedule(it.next(), startTime - System.currentTimeMillis() + RUNNABLE_ADD_TIME);
    }
    
    Thread.sleep(startTime - System.currentTimeMillis() + RUNNABLE_ADD_TIME);
    
    Thread.sleep(RUN_TIME);
    
    run = false;
    Thread.sleep(1000);
    shutdownScheduler();
    
    if (RUN_PROFILER) {
      p.stop();
    }
    
    int total = 0;
    StringBuilder result = new StringBuilder();
    for(int i = 0; i < RUNNABLE_COUNT; i++) {
      if (i != 0) {
        result.append(", ");
      }
      result.append(countArray.get(i));
      total += countArray.get(i);
    }
    //System.out.println(result.toString());
    System.out.println(this.getClass().getSimpleName() + OUTPUT_DELIM + total);
    
    if (RUN_PROFILER) {
      p.dump(System.out);
    }
  }
}
