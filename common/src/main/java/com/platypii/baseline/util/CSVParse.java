package com.platypii.baseline.util;

import android.util.Log;
import androidx.annotation.NonNull;
import java.text.ParseException;
import java.util.Date;

/**
 * Parse data from CSV files
 */
public class CSVParse {
    private static final String TAG = "CSVParse";

    /**
     * Return column value as double, else NaN
     *
     * @param row the split of the row
     * @param columns the csv header mapping
     * @param columnName the name of the column to retrieve
     */
    public static double getColumnDouble(@NonNull String[] row, @NonNull CSVHeader columns, @NonNull String columnName) {
        final Integer index = columns.get(columnName);
        if (index != null && index < row.length) {
            try {
                final String col = row[index];
                if (!col.isEmpty()) {
                    return Double.parseDouble(col);
                }
            } catch (Exception e) {
                Log.w(TAG, "CSV column double parsing exception", e);
            }
        }
        return Double.NaN;
    }

    /**
     * Return column value as long, else -1
     *
     * @param row the split of the row
     * @param columns the csv header mapping
     * @param columnName the name of the column to retrieve
     */
    public static long getColumnLong(@NonNull String[] row, @NonNull CSVHeader columns, @NonNull String columnName) {
        final Integer index = columns.get(columnName);
        if (index != null && index < row.length) {
            try {
                final String col = row[index];
                if (!col.isEmpty()) {
                    return Long.parseLong(col);
                }
            } catch (Exception e) {
                Log.w(TAG, "CSV column long parsing exception", e);
            }
        }
        return -1L;
    }

    /**
     * Return column value parsed as ISO date, else -1
     *
     * @param row the split of the row
     * @param columns the csv header mapping
     * @param columnName the name of the column to retrieve
     * @return date in milliseconds since the epoch
     */
    public static long getColumnDate(@NonNull String[] row, @NonNull CSVHeader columns, @NonNull String columnName) {
        final Integer index = columns.get(columnName);
        if (index != null && index < row.length) {
            try {
                final String col = row[index];
                if (!col.isEmpty()) {
                    return parseFlySightDate(col);
                }
            } catch (Exception e) {
                Log.w(TAG, "CSV column date parsing exception", e);
            }
        }
        return -1L;
    }

    /**
     * Return column value as string, else ""
     *
     * @param row the split of the row
     * @param columns the csv header mapping
     * @param columnName the name of the column to retrieve
     */
    @NonNull
    public static String getColumnString(@NonNull String[] row, @NonNull CSVHeader columns, @NonNull String columnName) {
        final Integer index = columns.get(columnName);
        if (index != null && index < row.length) {
            return row[index];
        } else {
            return "";
        }
    }

    /**
     * Return true if the column is "Y". Case sensiTive.
     */
    public static boolean getColumnYes(@NonNull String[] row, @NonNull CSVHeader columns, @NonNull String columnName) {
        final Integer index = columns.get(columnName);
        if (index != null && index < row.length) {
            return "Y".equals(row[index]);
        } else {
            return false;
        }
    }

    static long parseFlySightDate(@NonNull String dateString) throws ParseException {
        // 2018-01-25T11:48:09.80Z
        if (!Character.isDigit(dateString.charAt(0))) {
            return -1;
        }

        // Parse manually for speed
        final int year = Integer.parseInt(dateString.substring(0, 4)) - 1900;
        final int month = Integer.parseInt(dateString.substring(5, 7)) - 1;
        final int day = Integer.parseInt(dateString.substring(8, 10));
        final int hour = Integer.parseInt(dateString.substring(11, 13));
        final int minute = Integer.parseInt(dateString.substring(14, 16));
        final int second = Integer.parseInt(dateString.substring(17, 19));
        final long whole = Date.UTC(year, month, day, hour, minute, second);

        // Handle milliseconds separately
        long millis = 0;
        final int dotIndex = 19;
        if (dateString.charAt(dotIndex) == '.') {
            final int len = dateString.length();
            final int digits = Math.min(3, len - dotIndex - 2);
            final String zeros = "000".substring(digits);
            final String milliString = dateString.substring(dotIndex + 1, dotIndex + digits + 1) + zeros;
            millis = Long.parseLong(milliString);
        }
        return whole + millis;
    }
}
