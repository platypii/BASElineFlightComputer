package com.platypii.baseline.data;

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

	private LinkedList<T> toCopy = new LinkedList<T>();
	private LinkedList<T> values = new LinkedList<T>();
	private final int maxSize;
	

	public SyncedList(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public void addLast(T value) {
		synchronized(toCopy) {
			toCopy.addLast(value);
			// Trim toCopy
			while(maxSize < toCopy.size()) {
				toCopy.removeFirst();
	        }
		}
	}

	public Iterator<T> iterator() {
		copy();
		return values.iterator();
	}

	private void copy() {
		synchronized(toCopy) {
			values.addAll(toCopy);
			toCopy.clear();
		}
		// Trim values
		while(maxSize < values.size()) {
			values.removeFirst();
        }
	}
}
