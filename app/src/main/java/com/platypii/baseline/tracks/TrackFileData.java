package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Parse location data from track file
 */
public class TrackFileData {
    private static final String TAG = "TrackFileData";

    public static List<MLocation> getTrackData(File trackFile) {
        final List<MLocation> data = new ArrayList<>();
        // Read file line by line
        // TODO minsdk19: InputStreamReader(,StandardCharsets.UTF_8)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(trackFile))))) {
            // Parse header column
            String line = br.readLine();
            final Map<String,Integer> columns = new HashMap<>();
            final String[] header = line.split(",");
            for (int i = 0; i < header.length; i++) {
                columns.put(header[i], i);
            }
            // Add column aliases
            addMapping(columns, "timeMillis", "millis");
            // Handle old files that were not FlySight compatible
            addMapping(columns, "latitude", "lat");
            addMapping(columns, "longitude", "lon");
            addMapping(columns, "altitude_gps", "hMSL");

            // Parse data rows
            while ((line = br.readLine()) != null) {
                final String[] row = line.split(",");
                final Integer sensorIndex = columns.get("sensor");
                if (sensorIndex == null) {
                    // FlySight
                    final long millis = getColumnDate(row, columns, "time");
                    final double lat = getColumnDouble(row, columns, "lat");
                    final double lon = getColumnDouble(row, columns, "lon");
                    final double alt_gps = getColumnDouble(row, columns, "hMSL");
                    final double climb = -getColumnDouble(row, columns, "velD");
                    final double vN = getColumnDouble(row, columns, "velN");
                    final double vE = getColumnDouble(row, columns, "velE");
                    if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                        final MLocation loc = new MLocation(millis, lat, lon, alt_gps, climb, vN, vE, Float.NaN, Float.NaN, Float.NaN, Float.NaN, 0, 0);
                        data.add(loc);
                    }
                } else if (row[sensorIndex].equals("gps")) {
                    // BASEline GPS measurement
                    final long millis = getColumnLong(row, columns, "millis");
                    final double lat = getColumnDouble(row, columns, "lat");
                    final double lon = getColumnDouble(row, columns, "lon");
                    final double alt_gps = getColumnDouble(row, columns, "hMSL");
                    final double climb = -getColumnDouble(row, columns, "velD");
                    final double vN = getColumnDouble(row, columns, "velN");
                    final double vE = getColumnDouble(row, columns, "velE");
                    if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                        final MLocation loc = new MLocation(millis, lat, lon, alt_gps, climb, vN, vE, Float.NaN, Float.NaN, Float.NaN, Float.NaN, 0, 0);
                        data.add(loc);
                    }
                } else if (columns.containsKey("sensor") && row[columns.get("sensor")].equals("gps")) {
                    // TODO: Handle alti measurement
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting track data from " + trackFile, e);
        }
        return data;
    }

    private static void addMapping(Map<String,Integer> columns, String from, String to) {
        if (columns.containsKey(from) && !columns.containsKey(to)) {
            columns.put(to, columns.get(from));
        }
    }

    private static double getColumnDouble(String[] row, Map<String,Integer> columns, String columnName) {
        try {
            return Double.parseDouble(row[columns.get(columnName)]);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private static long getColumnLong(String[] row, Map<String,Integer> columns, String columnName) {
        try {
            return Long.parseLong(row[columns.get(columnName)]);
        } catch (Exception e) {
            return -1L;
        }
    }

    private static SimpleDateFormat df;
    private static long getColumnDate(String[] row, Map<String, Integer> columns, String columnName) {
        // Lazy init
        if (df == null) {
            df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ", Locale.US);
        }
        try {
            final String dateString = row[columns.get(columnName)];
            return df.parse(dateString).getTime();
        } catch (Exception e) {
            return -1L;
        }
    }
}
