package org.threadly.concurrent.benchmark;

import java.util.concurrent.RejectedExecutionException;

import org.threadly.util.ExceptionHandler;
import org.threadly.util.ExceptionUtils;

public abstract class AbstractBenchmark {
  protected static final int RUN_TIME = 1000 * 60;
  
  public static final String OUTPUT_DELIM = ": ";
  
  static {
    ExceptionUtils.setDefaultExceptionHandler(new ExceptionHandler() {
      @Override
      public void handleException(Throwable thrown) {
        if (thrown instanceof RejectedExecutionException) {
          // ignore
        } else {
          thrown.printStackTrace();
        }
      }
    });
  }
}
