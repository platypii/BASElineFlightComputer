package com.platypii.baseline.places;

import com.platypii.baseline.location.Geo;
import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Manages the place database
 */
public class Places {
    private static final String TAG = "Places";

    private static final long updateDuration = 24 * 60 * 60 * 1000; // Update if data is older than 1 day

    private File placeFile;

    // In-memory cache of places, lazy loaded on first call to getNearestPlace()
    private List<Place> places = null;

    public void start(@NonNull Context context) {
        // Update places in background
        AsyncTask.execute(() -> {
            // Place file is stored on internal storage
            placeFile = PlaceFile.placeFile(context);
            // Fetch places from server, if we need to
            if (!placeFile.exists() || placeFile.lastModified() < System.currentTimeMillis() - updateDuration) {
                try {
                    FetchPlaces.get(placeFile);
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
                places = PlaceFile.load(placeFile);
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

}
