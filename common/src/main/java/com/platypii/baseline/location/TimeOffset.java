package com.platypii.baseline.location;

import android.util.Log;

/**
 * Phone time is not necessarily the same as gps time.
 * Sensor readings come with phone time stamps.
 * This class stores the global state for tracking this clock difference.
 */
public class TimeOffset {

    // phone time = GPS time + offset
    private static long phoneOffsetMillis = 0;

    /**
     * Update the time offset based on a received gps signal
     * @param provider the name of the gps source so we can log
     * @param gpsTime gps time in milliseconds
     */
    public static void update(String provider, long gpsTime) {
        final long clockOffset = System.currentTimeMillis() - gpsTime;
        if (Math.abs(phoneOffsetMillis - clockOffset) > 1000) {
            if (clockOffset < 0) {
                Log.w(provider, "Adjusting clock: phone behind gps by " + (-clockOffset) + "ms");
            } else {
                Log.w(provider, "Adjusting clock: phone ahead of gps by " + clockOffset + "ms");
            }
        }
        phoneOffsetMillis = clockOffset;
    }

    public static long gpsToPhoneTime(long gpsTime) {
        return gpsTime + phoneOffsetMillis;
    }

    public static long phoneToGpsTime(long phoneTime) {
        return phoneTime - phoneOffsetMillis;
    }

}
