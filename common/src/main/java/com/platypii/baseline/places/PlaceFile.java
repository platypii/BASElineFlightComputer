package com.platypii.baseline.places;

import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Loads places from gzipped CSV
 */
class PlaceFile {
    private static final String TAG = "ParsePlaces";

    private static final String placeFilename = "places/places.csv.gz";
    private static final long ttl = 24 * 60 * 60 * 1000; // Update if data is older than 1 day

    final File file;

    PlaceFile(@NonNull Context context) {
        file = new File(context.getFilesDir(), placeFilename);
    }

    boolean exists() {
        return file.exists();
    }

    boolean isFresh() {
        return file.exists() && System.currentTimeMillis() < file.lastModified() + ttl;
    }

    /**
     * Parse places from local file into list of Places
     */
    @NonNull
    List<Place> parse() throws IOException {
        Log.i(TAG, "Loading places from file (" + (file.length()>>10) + " KiB)");
        final List<Place> places = new ArrayList<>();
        // Read place file csv (gzipped)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
            // Parse header column
            String line = br.readLine();
            final Map<String,Integer> columns = getColumns(line);
            // Parse data rows
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    try {
                        final String[] row = line.split(",");
                        final String name = row[columns.get("name")];
                        final String region = row[columns.get("region")];
                        final String country = row[columns.get("country")];
                        final double latitude = getColumnDouble(row, columns, "latitude");
                        final double longitude = getColumnDouble(row, columns, "longitude");
                        final double altitude = getColumnDouble(row, columns, "altitude");
                        final String objectType = row[columns.get("type")];
                        final double radius = getColumnDouble(row, columns, "radius");
                        places.add(new Place(name, region, country, latitude, longitude, altitude, objectType, radius));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing place file", e);
                    }
                }
            }
        }
        Log.i(TAG, "Loaded " + places.size() + " places");
        return places;
    }

    @NonNull
    private static Map<String,Integer> getColumns(String header) {
        final Map<String,Integer> columns = new HashMap<>();
        final String[] headers = header.split(",");
        for (int i = 0; i < headers.length; i++) {
            columns.put(headers[i], i);
        }
        return columns;
    }

    private static double getColumnDouble(String[] row, @NonNull Map<String,Integer> columns, String columnName) {
        try {
            return Numbers.parseDouble(row[columns.get(columnName)]);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

}
