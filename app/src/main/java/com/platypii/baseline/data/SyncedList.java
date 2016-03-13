package com.platypii.baseline.data;

import android.text.TextUtils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Acts like a LinkedList, but uses a copy queue so that adding never blocks.
 * Asynchronously copies elements when an iterator is required.
 * The caller should block on the SyncedList object while iterating.
 * This ensures that the sensor manager can never block during a sensor reading.
 * @author platypii
 */
public class SyncedList<T> implements Iterable<T> {

    private final LinkedList<T> toCopy = new LinkedList<>();
    private final LinkedList<T> values = new LinkedList<>();
    private final int maxSize;
    private int size = 0;

    // The last value appended
    private T lastValue;

    public SyncedList(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Adds a new value to the end of the list
     * @param value the new value to add
     */
    public void append(T value) {
        synchronized(toCopy) {
            lastValue = value;
            toCopy.addLast(value);
            // Trim toCopy
            while(maxSize < toCopy.size()) {
                toCopy.removeFirst();
                size--;
            }
        }
        size++;
    }

    /**
     * Return the last value in the list
     * @return the last value in the list
     */
    public T last() {
        return lastValue;
    }

    public Iterator<T> iterator() {
        copy();
        return values.iterator();
    }

    /**
     * Move from the toCopy list to the values list
     */
    private void copy() {
        synchronized(toCopy) {
            values.addAll(toCopy);
            toCopy.clear();
        }
        // Trim values
        while(maxSize < values.size()) {
            values.removeFirst();
            size--;
        }
    }

    /**
     * Returns the number of elements in this list
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "SyncedList(" + TextUtils.join(",", values) + ")";
    }
}
