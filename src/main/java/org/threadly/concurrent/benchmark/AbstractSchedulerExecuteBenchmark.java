package org.threadly.concurrent.benchmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.threadly.util.Clock;
import org.threadly.util.debug.Profiler;

public abstract class AbstractSchedulerExecuteBenchmark extends AbstractSchedulerBenchmark {
  private static volatile boolean run = true;
  private static final AtomicIntegerArray countArray = new AtomicIntegerArray(RUNNABLE_COUNT);
  
  protected void runTest() throws InterruptedException {
    run = true;
    
    List<TestRunnable> runnables = new ArrayList<TestRunnable>(RUNNABLE_COUNT);
    for (int i = 0; i < RUNNABLE_COUNT; i++) {
      runnables.add(new TestRunnable(i));
    }
    Profiler p;
    if (RUN_PROFILER) {
      p = new Profiler(10);
      p.start();
    }

    long startTime = Clock.accurateTimeMillis();
    Iterator<TestRunnable> it = runnables.iterator();
    while (it.hasNext()) {
      long delayTime = startTime - System.currentTimeMillis() + RUNNABLE_ADD_TIME;
      //System.out.println(delayTime);
      getScheduler().schedule(it.next(), delayTime);
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
    System.out.println(this.getClass().getSimpleName() + ": " + total);
    
    if (RUN_PROFILER) {
      p.dump(System.out);
    }
  }
  
  private class TestRunnable implements Runnable {
    private final int index;
    
    private TestRunnable(int index) {
      this.index = index;
    }

    @Override
    public void run() {
      if (run) {
        countArray.incrementAndGet(index);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < THREAD_RUN_TIME) {
          // spin loop
        }
        getScheduler().execute(this);
      }
    }
    
    @Override
    public String toString() {
      return Integer.toHexString(System.identityHashCode(this));
    }
  }
}
