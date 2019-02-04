package com.platypii.baseline.util;

import android.support.annotation.NonNull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Acts like a LinkedList, but uses a copy queue so that adding never blocks.
 * Asynchronously copies elements when an iterator is required.
 * The caller should block on the SyncedList object while iterating.
 * This ensures that the sensor manager can never block during a sensor reading.
 */
public class SyncedList<T> implements Iterable<T> {

    private final LinkedList<T> toCopy = new LinkedList<>();
    private final LinkedList<T> values = new LinkedList<>();
    private int maxSize = 0;
    private int size = 0;

    /**
     * Adds a new value to the end of the list
     * @param value the new value to add
     */
    public void append(T value) {
        if (maxSize > 0) {
            synchronized (toCopy) {
                toCopy.addLast(value);
                // Trim toCopy
                while (maxSize < toCopy.size()) {
                    toCopy.removeFirst();
                    size--;
                }
            }
            size++;
        }
    }

    @NonNull
    public Iterator<T> iterator() {
        copy();
        return values.iterator();
    }

    /**
     * Move from the toCopy list to the values list
     */
    private void copy() {
        if (maxSize > 0) {
            synchronized (toCopy) {
                values.addAll(toCopy);
                toCopy.clear();
            }
            // Trim values
            while (maxSize < values.size()) {
                values.removeFirst();
                size--;
            }
        } else {
            toCopy.clear();
            values.clear();
            size = 0;
        }
    }

    /**
     * Returns the number of elements in this list
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @NonNull
    @Override
    public String toString() {
        copy();
        // TODO: TextUtils.join works in android but not in testing
        String str = Arrays.toString(values.toArray());
        str = str.substring(1, str.length() - 1);
        return "SyncedList(" + str + ")";
    }
}
