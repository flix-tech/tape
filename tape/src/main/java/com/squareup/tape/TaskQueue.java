// Copyright 2012 Square, Inc.
// Copyright 2016 FlixMobility GmbH
package com.squareup.tape;

/**
 * Persistent task queue. Not safe for concurrent use.
 *
 * @param <T> The type of tasks in the queue.
 */
public class TaskQueue<T extends Task> implements ObjectQueue<T> {

  private final TaskInjector<T> taskInjector;
  private final ObjectQueue<T> delegate;

  public TaskQueue(ObjectQueue<T> delegate) {
    this(delegate, null);
  }

  public TaskQueue(ObjectQueue<T> delegate, TaskInjector<T> taskInjector) {
    this.delegate = delegate;
    this.taskInjector = taskInjector;
  }

  /**
   * {@inheritDoc}
   *
   * Overridden to inject members into Tasks.
   */
  @Override public T peek() {
    T task = delegate.peek();
    if (task != null && taskInjector != null) {
      taskInjector.injectMembers(task);
    }
    return task;
  }

  @Override public int size() {
    return delegate.size();
  }

  @Override public void add(T entry) {
    add(entry, 0, -1);
  }

  @Override public void add(T entry, long validUntil, int retryCount) {
    if (retryCount == 0) {
      retryCount = -1;
    }
    delegate.add(entry, validUntil, retryCount);
  }

  @Override public void remove() {
    delegate.remove();
  }

  @Override public boolean drop() {
    return delegate.drop();
  }

  @Override public void setListener(final Listener<T> listener) {
    if (listener != null) {
      // Intercept event delivery to pass the correct TaskQueue instance to listener.
      delegate.setListener(new Listener<T>() {
        @Override
        public void onAdd(ObjectQueue<T> queue, T entry) {
          listener.onAdd(TaskQueue.this, entry);
        }

        @Override
        public void onRemove(ObjectQueue<T> queue) {
          listener.onRemove(TaskQueue.this);
        }
      });
    } else {
      delegate.setListener(null);
    }
  }
}
