package org.threadly.concurrent.benchmark;

import java.security.SecureRandom;
import java.util.Random;

public abstract class AbstractBenchmark {
  protected static final long RUN_TIME = 1000 * 60;
  
  protected static final Random RANDOM = new SecureRandom();
}
