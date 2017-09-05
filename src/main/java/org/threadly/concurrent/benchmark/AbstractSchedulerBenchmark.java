package org.threadly.concurrent.benchmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.threadly.concurrent.SimpleSchedulerInterface;
import org.threadly.util.Clock;

public abstract class AbstractSchedulerBenchmark extends AbstractBenchmark {
  protected static final int RUNNABLE_COUNT = 10000;
  protected static final int RUNNABLE_PER_COUNTER = 2; // higher numbers use less ram but may have contention
  protected static final int RUNNABLE_ADD_TIME = 1000 * 15;
  
  protected final int threadRunTime;
  protected final AtomicIntegerArray countArray = 
      new AtomicIntegerArray(RUNNABLE_COUNT / RUNNABLE_PER_COUNTER);
  protected volatile boolean run = true;
  
  protected AbstractSchedulerBenchmark(int threadRunTime) {
    this.threadRunTime = threadRunTime;
  }
  
  protected abstract SimpleSchedulerInterface getScheduler();
  
  protected abstract void shutdownScheduler();
  
  protected abstract Runnable makeRunnable(int id);
  
  protected void initialSchedule(Runnable r, long delayInMs) {
    getScheduler().schedule(r, delayInMs);
  }
  
  protected void runTest() throws InterruptedException {
    run = true;
    
    List<Runnable> runnables = new ArrayList<Runnable>(RUNNABLE_COUNT);
    for (int i = 0; i < RUNNABLE_COUNT; i++) {
      runnables.add(makeRunnable(i % countArray.length()));
    }

    Iterator<Runnable> it = runnables.iterator();
    long startTime = Clock.accurateTime();
    while (it.hasNext()) {
      initialSchedule(it.next(), startTime - Clock.accurateTime() + RUNNABLE_ADD_TIME);
    }
    
    Thread.sleep((startTime - Clock.accurateTime()) + RUNNABLE_ADD_TIME);
    
    Thread.sleep(RUN_TIME);
    
    run = false;
    Thread.sleep(1000);
    shutdownScheduler();
    
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
        if (Clock.accurateTime() - startReferenceTime >= threadRunTime) {
          break;
        } else {
          timeCheckIterations = Math.max(10, timeCheckIterations / 2);
          i = 0;
        }
      }
    }
  }
}
