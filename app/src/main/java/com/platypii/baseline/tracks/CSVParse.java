package com.platypii.baseline.tracks;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Parse location data from track file
 */
class CSVParse {

    static void addMapping(Map<String, Integer> columns, String from, String to) {
        if (columns.containsKey(from) && !columns.containsKey(to)) {
            columns.put(to, columns.get(from));
        }
    }

    static double getColumnDouble(String[] row, @NonNull Map<String, Integer> columns, String columnName) {
        try {
            return Double.parseDouble(row[columns.get(columnName)]);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    static long getColumnLong(String[] row, @NonNull Map<String, Integer> columns, String columnName) {
        try {
            return Long.parseLong(row[columns.get(columnName)]);
        } catch (Exception e) {
            return -1L;
        }
    }

    static long getColumnDate(String[] row, @NonNull Map<String, Integer> columns, String columnName) {
        try {
            final String dateString = row[columns.get(columnName)];
            return parseFlySightDate(dateString);
        } catch (Exception e) {
            return -1L;
        }
    }

    private static SimpleDateFormat df;
    private static long parseFlySightDate(@NonNull String dateString) throws ParseException {
        // Lazy init
        if (df == null) {
            // 2018-01-25T11:48:09.80Z
            df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        // Handle milliseconds separately
        long millis = 0;
        final int len = dateString.length();
        if (dateString.charAt(len - 4) == '.') {
            millis = 10 * Long.parseLong(dateString.substring(len - 3, len - 1));
        }
        return df.parse(dateString).getTime() + millis;
    }
}
