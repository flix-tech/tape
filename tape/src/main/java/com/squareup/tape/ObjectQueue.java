// Copyright 2011 Square, Inc.
// Copyright 2016 FlixMobility GmbH
package com.squareup.tape;

/**
 * A queue of objects.
 *
 * @param <T> The type of queue for the elements.
 */
public interface ObjectQueue<T> {

  /** Returns the number of entries in the queue. */
  int size();

  /** Enqueues an entry that can be processed at any time. */
  void add(T entry);

  /** Enqueues an entry that can be processed at any time. */
  void add(T entry, long validUntil, int retryCount);

  /**
   * Returns the head of the queue, or {@code null} if the queue is empty. Does not modify the
   * queue.
   */
  T peek();

  /** Removes the head of the queue. */
  void remove();

  /** Drops the head of the queue if policy is active. */
  boolean drop();

  /** Clears the queue completely */
  void clear();

  /**
   * Sets a listener on this queue. Invokes {@link Listener#onAdd} once for each entry that's
   * already in the queue. If an error occurs while reading the data, the listener will not receive
   * further notifications.
   */
  void setListener(Listener<T> listener);

  /**
   * Listens for changes to the queue.
   *
   * @param <T> The type of elements in the queue.
   */
  public interface Listener<T> {

    /** Called after an entry is added. */
    void onAdd(ObjectQueue<T> queue, T entry);

    /** Called after an entry is removed. */
    void onRemove(ObjectQueue<T> queue);
  }
}
