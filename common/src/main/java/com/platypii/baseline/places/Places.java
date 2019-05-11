package com.platypii.baseline.places;

import com.platypii.baseline.BaseService;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLngBounds;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the place database
 */
public class Places implements BaseService {
    private static final String TAG = "Places";

    private PlaceFile placeFile;

    public final NearestPlace nearestPlace = new NearestPlace(this);

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
    @Nullable
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

    @NonNull
    public List<Place> getPlacesByArea(@NonNull LatLngBounds bounds) {
        final long start = System.currentTimeMillis();
        final List<Place> filtered = new ArrayList<>();
        final List<Place> places = getPlaces();
        if (places != null) {
            for (Place place : places) {
                if (bounds.contains(place.latLng())) {
                    filtered.add(place);
                }
            }
            final long duration = System.currentTimeMillis() - start;
            Log.i(TAG, "Got " + filtered.size() + "/" + places.size() + " places in view " + duration + " ms");
        }
        return filtered;
    }

    @Override
    public void stop() {}

}
