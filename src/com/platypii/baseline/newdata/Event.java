package com.platypii.baseline.newdata;


/**
 * Generic event
 * @author platypii
 */
public abstract class Event {

	public static final int TYPE_ALTITUDE = 0;
	public static final int TYPE_LOCATION = 1;
	public static final int TYPE_JUMP = 2;

	public abstract int getType();
	
}
