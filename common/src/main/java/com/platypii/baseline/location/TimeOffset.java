package com.platypii.baseline.location;

import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Phone time is not necessarily the same as gps time.
 * Sensor readings come with phone time stamps.
 * This class stores the global state for tracking this clock difference.
 */
public class TimeOffset {
    // If phone time is different than GPS time by at least adjustThreshold, then we will adjust.
    // If this time is too short, then lag can cause frequent time shifts and messes up the data.
    private static final long adjustThreshold = 60000; // milliseconds
    private static final long warnThreshold = 1000; // milliseconds

    // phone time = GPS time + offset
    private static long phoneOffsetMillis = 0;
    private static boolean initialized = false;

    /**
     * Update the time offset based on a received gps signal
     * @param provider the name of the gps source so we can log
     * @param gpsTime gps time in milliseconds
     */
    public static void update(@NonNull String provider, long gpsTime) {
        final long clockOffset = System.currentTimeMillis() - gpsTime;
        if (!initialized) {
            Log.i(provider, "Initial time offset: " + offsetString(clockOffset));
            phoneOffsetMillis = clockOffset;
            initialized = true;
        } else if (clockOffset - phoneOffsetMillis > adjustThreshold) {
            Log.w(provider, "Adjusting time offset backward: " + offsetString(clockOffset) + " (" + (phoneOffsetMillis - clockOffset) + ")");
            phoneOffsetMillis = clockOffset;
        } else if (clockOffset - phoneOffsetMillis < 0) {
            // Adjusted phone time should never be behind gps time
            Log.w(provider, "Adjusting time offset forward: " + offsetString(clockOffset) + " (+" + (phoneOffsetMillis - clockOffset) + ")");
            phoneOffsetMillis = clockOffset;
        } else if (Math.abs(phoneOffsetMillis - clockOffset) > warnThreshold) {
            Log.w(provider, "Warning time offset: " + offsetString(clockOffset));
        }
    }

    private static String offsetString(long clockOffset) {
        if (clockOffset < 0) {
            return "phone behind gps by " + (-clockOffset) + "ms";
        } else {
            return "phone ahead of gps by " + clockOffset + "ms";
        }
    }

    public static long gpsToPhoneTime(long gpsTime) {
        return gpsTime + phoneOffsetMillis;
    }

    public static long phoneToGpsTime(long phoneTime) {
        return phoneTime - phoneOffsetMillis;
    }

}
