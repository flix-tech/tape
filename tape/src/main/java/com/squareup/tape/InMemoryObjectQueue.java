// Copyright 2012 Square, Inc.
// Copyright 2016 FlixMobility GmbH
package com.squareup.tape;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A queue for objects that are not serious enough to be written to disk.  Objects in this queue
 * are kept in memory and will not be serialized.
 *
 * @param <T> The type of elements in the queue.
 */
public class InMemoryObjectQueue<T> implements ObjectQueue<T> {
  private final Queue<Entry<T>> tasks;
  private Listener<T> listener;

  @SuppressWarnings("unchecked")
  public InMemoryObjectQueue() {
    tasks = (Queue<Entry<T>>) new LinkedList();
  }

  @Override public void add(T entry) {
    add(entry, 0, -1);
  }

  @Override public void add(T entry, long validUntil, int retryCount) {
    if (retryCount == 0) retryCount = -1;
    tasks.add(new Entry<T>(entry, validUntil, retryCount));
    if (listener != null) listener.onAdd(this, entry);
  }

  @Override public T peek() {
    Entry<T> wrapper = tasks.peek();

    if (wrapper == null) {
      return null;
    }

    if (wrapper.validUntil > 0 && wrapper.validUntil < System.currentTimeMillis()) {
      remove();
      return peek();
    }

    return wrapper.entry;
  }

  @Override public int size() {
    return tasks.size();
  }

  @Override public void remove() {
    tasks.remove();
    if (listener != null) listener.onRemove(this);
  }

  @Override public boolean drop() {
    Entry<T> wrapper = tasks.peek();
    if (wrapper == null) return false;
    boolean shouldRemove = wrapper.retryCount > 0 && --wrapper.retryCount == 0;
    shouldRemove |= wrapper.validUntil > 0 && wrapper.validUntil < System.currentTimeMillis();
    if (shouldRemove) remove();
    return shouldRemove;
  }

  @Override public void setListener(Listener<T> listener) {
    if (listener != null) {
      for (Entry<T> task : tasks) {
        listener.onAdd(this, task.entry);
      }
    }
    this.listener = listener;
  }

  private static class Entry<T> {
    T entry;
    long validUntil;
    int retryCount;

    Entry(T entry, long validUntil, int retryCount) {
      this.entry = entry;
      this.validUntil = validUntil;
      this.retryCount = retryCount;
    }
  }
}
