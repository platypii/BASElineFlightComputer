package com.platypii.baseline.location;

import com.platypii.baseline.util.Exceptions;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
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
    static double parseDegreesMinutes(@NonNull String dm, @NonNull String nsew) {
        if (dm.isEmpty()) {
            return Double.NaN;
        } else {
            final int index = dm.indexOf('.') - 2;
            if (index < 0) {
                Exceptions.report(new NMEAException("NMEA lat/lon parse error missing decimal: " + dm + " " + nsew));
                return Double.NaN;
            } else {
                try {
                    final double m = Double.parseDouble(dm.substring(index));
                    final int d = (index == 0) ? 0 : Integer.parseInt(dm.substring(0, index));
                    final double degrees = d + m / 60.0;

                    if (nsew.equalsIgnoreCase("S") || nsew.equalsIgnoreCase("W"))
                        return -degrees;
                    else
                        return degrees;
                } catch (Exception e) {
                    Exceptions.report(new NMEAException("NMEA lat/lon parse error: " + dm + " " + nsew));
                    return Double.NaN;
                }
            }
        }
    }

    /**
     * Parse DDMMYY into milliseconds since epoch
     */
    static long parseDate(@Nullable String date) {
        if (date == null || date.isEmpty()) {
            return 0;
        } else {
            if (date.length() == 6) {
                final int day = Integer.parseInt(date.substring(0, 2));
                final int month = Integer.parseInt(date.substring(2, 4)) - 1; // january is 0 not 1
                int year = 1900 + Integer.parseInt(date.substring(4, 6));
                if (year < 1970) year += 100;
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(year, month, day, 0, 0, 0);
                return cal.getTime().getTime();
            } else {
                Log.e(TAG, "Date format error " + date);
                return 0;
            }
        }
    }

    /**
     * Parse HHMMSS.SS UTC time into milliseconds since midnight
     */
    static long parseTime(@Nullable String time) {
        if (time == null || time.isEmpty()) {
            return 0;
        } else {
            try {
                if (time.indexOf('.') != 6) {
                    Log.e(TAG, "Time format error " + time);
                }
                final long hour = Integer.parseInt(time.substring(0, 2));
                final long min = Integer.parseInt(time.substring(2, 4));
                // double sec = Double.parseDouble(utc.substring(4));
                final long sec = Integer.parseInt(time.substring(4, 6));
                final long ms = time.length() <= 6 ? 0 : (long) (1000 * Double.parseDouble(time.substring(6)));
                return hour * 3600000 + min * 60000 + sec * 1000 + ms;
            } catch (Exception e) {
                return 0;
            }
        }
    }

    /**
     * Returns true if the checksum is valid
     */
    static void validate(@NonNull String nmea) throws NMEAException {
        final int starIndex = nmea.lastIndexOf('*');
        final int length = nmea.length();
        // Ensure that:
        // - string is long enough
        // - starts with $
        // - ends with checksum
        // Could use regex ^\\$.*\\*[0-9a-fA-F]{2} but this is faster:
        if (length < 8 || nmea.charAt(0) != '$' || starIndex != length - 3) {
            if (nmea.startsWith("$PGLOR,") || nmea.startsWith("$AIDSTAT,")) {
                // Some commands omit or truncate checksum, no need to report it
                return;
            } else {
                throw new NMEAException("Invalid NMEA sentence: " + nmea);
            }
        }
        // Special commands that don't checksum
        if (nmea.startsWith("$AIDSTAT") && nmea.endsWith("*00")) return;
        if (nmea.startsWith("$ENGINESTATE") && nmea.endsWith("*00")) return;

        // Compute checksum
        short checksum1 = 0;
        for (int i = 1; i < starIndex; i++) {
            checksum1 ^= nmea.charAt(i);
        }
        final short checksum2 = Short.parseShort(nmea.substring(starIndex + 1), 16);
        if (checksum1 != checksum2) {
            throw new NMEAChecksumException(String.format(Locale.US, "Invalid NMEA checksum: %02X != %02X for sentence: %s", checksum1, checksum2, nmea));
        }
    }

    /**
     * Convert Dual XGPS voltage into battery level %.
     * Inspired by code from XGPS160API.m
     * TODO: Voltage not valid while charging
     *
     * Dual proprietary sentence for battery level:
     * $GPPWR,04C3,0,0,0,0,00,0,0,97, 1 9 ,S00 // not charging 04C3 = 1219 = ~70%
     * $GPPWR,0501,1,0,1,1,00,0,0,97, 1 9 ,S00 // charging
     */
    static float parsePowerLevel(@NonNull String[] split) {
        if (!"$GPPWR".equals(split[0])) {
            Exceptions.report(new IllegalStateException("Parse power level should only be called on GPPWR"));
        }
        try {
            // Parse voltage from split[1] as hexadecimal
            final int voltage = Integer.parseInt(split[1], 16);
            // Voltage ranges from 1100 to 1280
            final float batteryLevel = (voltage - 1091) / (1280f - 1091f);
            // Restrict range from 0 to 100%
            return Math.max(0f, Math.min(batteryLevel, 1f));
        } catch (Exception e) {
            Exceptions.report(e);
            return Float.NaN;
        }
    }

    /**
     * Remove junk before and after nmea sentence
     */
    @NonNull
    static String cleanNmea(@NonNull String nmea) {
        // Remove anything before $
        final int sentenceStart = nmea.indexOf('$');
        if (sentenceStart > 0) {
            nmea = nmea.substring(sentenceStart);
        }
        // Trim whitespace and \0
        return nmea.trim();
    }

    /**
     * Split nmea sentence into columns, no checksum
     */
    @NonNull
    static String[] splitNmea(@NonNull String nmea) {
        // Strip checksum
        final int starIndex = nmea.lastIndexOf('*');
        if (0 < starIndex && starIndex < nmea.length()) {
            nmea = nmea.substring(0, starIndex);
        }
        // Split on comma, -1 preserves trailing columns
        return nmea.split(",", -1);
    }

}
