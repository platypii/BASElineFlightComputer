package com.platypii.baseline.places;

import com.platypii.baseline.location.Geo;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the place database
 */
public class Places {
    private static final String TAG = "Places";

    private static final String placesUrl = "https://baseline.ws/assets/dropzones.csv";
    private static final long updateDuration = 24 * 60 * 60 * 1000; // Update if data is older than 1 day

    private File placeFile;

    // In-memory cache of places, lazy loaded on first call to getNearestPlace()
    private List<Place> places = null;

    public void start(@NonNull Context context) {
        // Update places in background
        AsyncTask.execute(() -> {
            // Place file is stored on internal storage
            placeFile = new File(context.getFilesDir(), "places/places.csv");
            // Fetch places from server, if we need to
            if (!placeFile.exists() || placeFile.lastModified() < System.currentTimeMillis() - updateDuration) {
                try {
                    fetchPlaces(placeFile);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to fetch places", e);
                }
            }
        });
    }

    /**
     * Find the closest place to the given location
     */
    @Nullable
    public Place getNearestPlace(@NonNull MLocation loc) {
        // Load from place file, if necessary
        if (places == null && placeFile != null && placeFile.exists()) {
            try {
                places = loadPlaces(placeFile);
            } catch (IOException e) {
                Log.e(TAG, "Error loading places", e);
            }
        }
        if (places != null) {
            Place best = null;
            double best_distance = Double.NaN;
            // Find closest place
            for (Place place : places) {
                final double distance = Geo.distance(loc.latitude, loc.longitude, place.latitude, place.longitude);
                if (Double.isNaN(best_distance) || distance < best_distance) {
                    best = place;
                    best_distance = distance;
                }
            }
            return best;
        } else {
            return null;
        }
    }

    /**
     * Load places from local cache
     */
    private static List<Place> loadPlaces(File placeFile) throws IOException {
        Log.i(TAG, "Loading places from file (" + (placeFile.length() / 1024) + "KiB)");
        final List<Place> places = new ArrayList<>();
        // Read place file csv
        try (BufferedReader br = new BufferedReader(new FileReader(placeFile))) {
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

    /**
     * Fetch places from BASEline server and saves it as a file
     */
    private static void fetchPlaces(File placeFile) throws IOException {
        Log.i(TAG, "Downloading places");
        final URL url = new URL(placesUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Make places directory
                placeFile.getParentFile().mkdir();
                // Read body to place file
                final OutputStream os = new FileOutputStream(placeFile);
                IOUtil.copy(conn.getInputStream(), os);
                Log.i(TAG, "Place file download successful");
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

}