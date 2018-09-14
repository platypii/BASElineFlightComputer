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

    File file;

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
    List<Place> parse() throws IOException {
        Log.i(TAG, "Loading places from file (" + (file.length() / 1024) + "KiB)");
        final List<Place> places = new ArrayList<>();
        // Read place file csv (gzipped)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
            // Parse header column
            String line = br.readLine();
            final Map<String,Integer> columns = new HashMap<>();
            final String[] header = line.split(",");
            for (int i = 0; i < header.length; i++) {
                columns.put(header[i], i);
            }
            // Parse data rows
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    try {
                        final String[] row = line.split(",");
                        final String name = row[columns.get("name")];
                        final String region = row[columns.get("region")];
                        final String country = row[columns.get("country")];
                        final double latitude = Numbers.parseDouble(row[columns.get("latitude")]);
                        final double longitude = Numbers.parseDouble(row[columns.get("longitude")]);
                        final double altitude = Numbers.parseDouble(row[columns.get("altitude")]);
                        final String objectType = row[columns.get("type")];
                        places.add(new Place(name, region, country, latitude, longitude, altitude, objectType));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing place file", e);
                    }
                }
            }
        }
        Log.i(TAG, "Loaded " + places.size() + " places");
        return places;
    }

}
