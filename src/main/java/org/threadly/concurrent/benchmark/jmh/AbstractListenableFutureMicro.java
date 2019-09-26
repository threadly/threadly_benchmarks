package org.threadly.concurrent.benchmark.jmh;

import java.util.function.Consumer;
import java.util.function.Function;

import org.threadly.util.StackSuppressedRuntimeException;

public class AbstractListenableFutureMicro {
  protected static final Consumer<Object> RESULT_CALLBACK = (ignored) -> { /* nothing */ };
  protected static final Consumer<Throwable> FAILURE_CALLBACK = (t) -> { /* nothing */ };
  protected static final Function<Object, Void> MAPPRER = (ignored) -> null;
  protected static final Function<Throwable, Void> FAILURE_MAPPRER = (t) -> null;
  protected static final Exception FAILURE = new StackSuppressedRuntimeException();
}
