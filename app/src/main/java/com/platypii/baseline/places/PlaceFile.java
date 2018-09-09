package com.platypii.baseline.places;

import com.platypii.baseline.util.Numbers;
import android.content.Context;
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

    private static final String placeFilename = "places/places.csv";

    static File placeFile(Context context) {
        return new File(context.getFilesDir(), placeFilename);
    }

    /**
     * Load places from local file
     */
    static List<Place> load(File placeFile) throws IOException {
        Log.i(TAG, "Loading places from file (" + (placeFile.length() / 1024) + "KiB)");
        final List<Place> places = new ArrayList<>();
        // Read place file csv (gzipped)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(placeFile))))) {
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
