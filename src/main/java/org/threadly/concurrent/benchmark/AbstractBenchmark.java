package org.threadly.concurrent.benchmark;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.RejectedExecutionException;

public abstract class AbstractBenchmark {
  protected static final int RUN_TIME = 1000 * 60;
  
  protected static final Random RANDOM = new SecureRandom();
  
  protected static final String OUTPUT_DELIM = ": ";
  
  static {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread thread, Throwable thrown) {
        if (thrown instanceof RejectedExecutionException || 
            (thrown instanceof IllegalStateException && thrown.getMessage().contains("shutdown"))) {
          // ignore
        } else {
          thrown.printStackTrace();
        }
      }
    });
  }
}
