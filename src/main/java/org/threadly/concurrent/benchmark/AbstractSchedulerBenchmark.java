package org.threadly.concurrent.benchmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.threadly.concurrent.SubmitterScheduler;
import org.threadly.util.Clock;
import org.threadly.util.debug.Profiler;

public abstract class AbstractSchedulerBenchmark extends AbstractBenchmark {
  protected static final int RUNNABLE_COUNT = 10000;
  protected static final int RUNNABLE_PER_COUNTER = 2; // higher numbers use less ram but may have contention
  protected static final int RUNNABLE_ADD_TIME = 1000 * 15;
  protected static final boolean RUN_PROFILER = false;
  
  protected final int threadRunTime;
  protected final AtomicIntegerArray countArray = new AtomicIntegerArray(RUNNABLE_COUNT / RUNNABLE_PER_COUNTER);
  protected volatile boolean run = true;
  
  protected AbstractSchedulerBenchmark(int threadRunTime) {
    this.threadRunTime = threadRunTime;
  }
  
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
      int index = i;
      while (index >= countArray.length()) {
        index -= countArray.length();
      }
      runnables.add(makeRunnable(index));
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
    
    long total = 0;
    StringBuilder result = new StringBuilder();
    for(int i = 0; i < countArray.length(); i++) {
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
  
  /**
   * Small function to simulate threads actually doing something.
   * 
   * @param startReferenceTime Time thread started running (should be collected at absolute start)
   */
  protected void doThreadWork(long startReferenceTime) {
    if (threadRunTime <= 0) {
      return;
    }
    
    int timeCheckIterations = 128;  // start high then decrease after first run
    int i = 0; // used to batch clock calls
    while (true) {
      // do some fake work, just to slow down clock calls without yielding the thread
      if (i % 2 == 0) {
        Math.pow(1024, 1024);
      } else {
        Math.log1p(1024);
      }
      if (Clock.lastKnownTimeMillis() - startReferenceTime >= threadRunTime) {
        break;
      } else if (++i == timeCheckIterations) {
        if (Clock.accurateTimeMillis() - startReferenceTime >= threadRunTime) {
          break;
        } else {
          timeCheckIterations = Math.max(10, timeCheckIterations / 2);
          i = 0;
        }
      }
    }
  }
}
