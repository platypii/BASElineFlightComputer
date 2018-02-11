package com.platypii.baseline.tracks;

import com.platypii.baseline.altimeter.BaroAltimeter;
import com.platypii.baseline.altimeter.Filter;
import com.platypii.baseline.altimeter.FilterKalman;
import com.platypii.baseline.measurements.MLocation;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

/**
 * Parse location data from track file
 */
public class TrackFileData {
    private static final String TAG = "TrackFileData";

    public static List<MLocation> getTrackData(File trackFile) {
        // Altitude kalman filters
        final Filter baroAltitudeFilter = new FilterKalman();
        final Filter gpsAltitudeFilter = new FilterKalman();

        long baroLastNano = -1L;
        long gpsLastMillis = -1L;

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
                    final double vN = getColumnDouble(row, columns, "velN");
                    final double vE = getColumnDouble(row, columns, "velE");
                    // Update gps altitude filter
                    if (gpsLastMillis < 0) {
                        gpsAltitudeFilter.init(alt_gps, 0);
                    } else {
                        final double dt = (millis - gpsLastMillis) * 0.001;
                        gpsAltitudeFilter.update(alt_gps, dt);
                    }
                    gpsLastMillis = millis;
                    // CLimb from baro or gps
                    final double climb;
                    if (Double.isNaN(baroAltitudeFilter.v)) {
                        climb = gpsAltitudeFilter.v;
                    } else {
                        climb = baroAltitudeFilter.v;
                    }
                    if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                        final MLocation loc = new MLocation(millis, lat, lon, alt_gps, climb, vN, vE, Float.NaN, Float.NaN, Float.NaN, Float.NaN, 0, 0);
                        data.add(loc);
                    }
                } else if (columns.containsKey("sensor") && row[columns.get("sensor")].equals("alt")) {
                    // BASEline alti measurement
                    final long nano = getColumnLong(row, columns, "nano");
                    final double pressure = getColumnDouble(row, columns, "pressure");
                    final double pressureAltitude = BaroAltimeter.pressureToAltitude(pressure);
                    if (baroLastNano < 0) {
                        baroAltitudeFilter.init(pressureAltitude, 0);
                    } else {
                        final double dt = (nano - baroLastNano) * 1E-9;
                        baroAltitudeFilter.update(pressureAltitude, dt);
                    }
                    baroLastNano = nano;
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
        try {
            final String dateString = row[columns.get(columnName)];
            return parseFlySightDate(dateString);
        } catch (Exception e) {
            return -1L;
        }
    }
    static long parseFlySightDate(String dateString) throws ParseException {
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
