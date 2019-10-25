package org.threadly.concurrent;

import java.util.Queue;

import org.threadly.concurrent.AbstractPriorityScheduler.OneTimeTaskWrapper;
import org.threadly.concurrent.AbstractPriorityScheduler.QueueSet;
import org.threadly.concurrent.AbstractPriorityScheduler.TaskWrapper;

public class ProtectedAccessor {
  public static Queue<? extends TaskWrapper> executeQueue(QueueSet qs) {
    return qs.executeQueue;
  }
  
  public static void resetStatus(OneTimeTaskWrapper taskWrapper) {
    taskWrapper.executed = false;
  }
}