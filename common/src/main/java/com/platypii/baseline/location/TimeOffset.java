package com.platypii.baseline.location;

/**
 * Phone time is not necessarily the same as gps time.
 * Sensor reading come with phone time stamps.
 * This class stores the global state for tracking this clock difference.
 */
public class TimeOffset {

    // phone time = GPS time + offset
    public static long phoneOffsetMillis = 0;

}
