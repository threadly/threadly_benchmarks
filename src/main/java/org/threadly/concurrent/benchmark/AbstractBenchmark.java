package org.threadly.concurrent.benchmark;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.RejectedExecutionException;

import org.threadly.util.ExceptionHandlerInterface;
import org.threadly.util.ExceptionUtils;

public abstract class AbstractBenchmark {
  protected static final int RUN_TIME = 1000 * 60;
  
  protected static final Random RANDOM = new SecureRandom();
  
  protected static final String OUTPUT_DELIM = ": ";
  
  static {
    ExceptionUtils.setDefaultExceptionHandler(new ExceptionHandlerInterface() {
      @Override
      public void handleException(Throwable thrown) {
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
