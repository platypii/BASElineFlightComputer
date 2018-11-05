package com.platypii.baseline.places;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.location.Geo;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.util.List;

/**
 * Manages the place database
 */
public class Places implements BaseService {
    private static final String TAG = "Places";

    private PlaceFile placeFile;

    public NearestPlace nearestPlace = new NearestPlace(this);

    // In-memory cache of places, lazy loaded on first call to getPlaces()
    @Nullable
    private List<Place> places = null;

    @Override
    public void start(@NonNull Context context) {
        // Update places in background
        AsyncTask.execute(() -> {
            // Place file is stored on internal storage
            placeFile = new PlaceFile(context);
            // Fetch places from server, if we need to
            if (!placeFile.isFresh()) {
                try {
                    FetchPlaces.get(placeFile.file);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to fetch places", e);
                }
            } else {
                Log.i(TAG, "Places file is already fresh");
            }
        });
    }

    /**
     * Load from place file, if necessary
     */
    List<Place> getPlaces() {
        if (places == null && placeFile != null && placeFile.exists()) {
            try {
                places = placeFile.parse();
                Log.i(TAG, "Loaded " + places.size() + " places");
            } catch (IOException e) {
                Log.e(TAG, "Error loading places", e);
            }
        }
        return places;
    }

    public String getNearestPlaceString(@NonNull MLocation loc) {
        final Place place = nearestPlace.cached(loc);
        if (place != null) {
            final double distance = Geo.distance(loc.latitude, loc.longitude, place.latitude, place.longitude);
            return String.format("%s (%s)", place, Convert.distance3(distance));
        } else {
            return "";
        }
    }

    @Override
    public void stop() {}

}
