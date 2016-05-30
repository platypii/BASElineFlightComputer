package com.platypii.baseline.location;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * NMEA decoding functions
 */
class NMEA {
    private static final String TAG = "NMEA";

    private static final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    /**
     * Parse DDDMM.MMMM,N into decimal degrees
     * @param dm The latitude or longitude in "DDDMM.MMMM" format
     * @param nsew The modifier "N", "S", "E", or "W"
     * @return The latitude or longitude in decimal degrees
     */
    static double parseDegreesMinutes(String dm, String nsew) {
        if(dm.equals("")) {
            return Double.NaN;
        } else {
            final int index = dm.indexOf('.') - 2;
            if(index < 0) {
                Log.e(TAG, "Lat/Long format error " + dm + " " + nsew);
            }
            final double m = Double.parseDouble(dm.substring(index));
            final int d = (index == 0)? 0 : Integer.parseInt(dm.substring(0, index));
            final double degrees = d + m / 60.0;

            if(nsew.equalsIgnoreCase("S") || nsew.equalsIgnoreCase("W"))
                return -degrees;
            else
                return degrees;
        }
    }

    /**
     * Parse DDMMYY into milliseconds since epoch
     */
    static long parseDate(String date) {
        if(date == null || date.equals("")) {
            return 0;
        } else {
            if(date.length() != 6) {
                Log.e(TAG, "Date format error " + date);
            }
            final int day = Integer.parseInt(date.substring(0, 2));
            final int month = Integer.parseInt(date.substring(2, 4)) - 1; // january is 0 not 1
            int year = 1900 + Integer.parseInt(date.substring(4, 6));
            if(year < 1970) year += 100;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(year, month, day, 0, 0, 0);
            return cal.getTime().getTime();
        }
    }

    /**
     * Parse HHMMSS.SS UTC time into milliseconds since midnight
     */
    static long parseTime(String time) {
        if(time == null || time.equals("")) {
            return 0;
        } else {
            try {
                if(time.indexOf('.') != 6) {
                    Log.e(TAG, "Time format error " + time);
                }
                final long hour = Integer.parseInt(time.substring(0, 2));
                final long min = Integer.parseInt(time.substring(2, 4));
                // double sec = Double.parseDouble(utc.substring(4));
                final long sec = Integer.parseInt(time.substring(4, 6));
                final long ms = time.length() <= 6? 0 : (long) (1000 * Double.parseDouble(time.substring(6)));
                return hour * 3600000 + min * 60000 + sec * 1000 + ms;
            } catch(Exception e) {
                return 0;
            }
        }
    }

    /** Returns true if the checksum is valid */
    static boolean validate(@NonNull String nmea) {
        int starIndex = nmea.indexOf('*');
        if(nmea.length() < 8 || nmea.charAt(0) != '$' || nmea.charAt(6) != ',' || starIndex == -1) {
            Log.e(TAG, "Invalid NMEA sentence: " + nmea);
            return false;
        }

        // Compute checksum
        short checksum1 = 0;
        for(int i = 1; i < starIndex; i++) {
            checksum1 ^= nmea.charAt(i);
        }
        final short checksum2 = Short.parseShort(nmea.substring(starIndex + 1, starIndex + 3), 16);
        if(checksum1 != checksum2) {
            Log.e(TAG, "Invalid NMEA checksum: " + nmea);
            return false;
        }
        return true;
    }

}
