package com.platypii.baseline.places;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.util.Exceptions;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLngBounds;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Manages the place database
 */
public class Places implements BaseService {
    private static final String TAG = "Places";

    @Nullable
    private Context context;
    @Nullable
    private PlaceFile placeFile;

    public final NearestPlace nearestPlace = new NearestPlace(this);

    // In-memory cache of places, lazy loaded on first call to getPlaces()
    @Nullable
    private List<Place> places = null;

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        updateAsync(false);
        EventBus.getDefault().register(this);
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

    /**
     * Update places in background thread
     */
    private void updateAsync(boolean force) {
        final Context ctx = context;
        if (ctx == null) {
            Exceptions.report(new NullPointerException("Null context in Places.updateAsync(" + force + ")"));
            return;
        }
        AsyncTask.execute(() -> {
            if (placeFile == null) {
                // Place file is stored on internal storage
                placeFile = new PlaceFile(ctx);
            }
            // Fetch places from server, if we need to
            if (force || !placeFile.isFresh()) {
                try {
                    FetchPlaces.get(ctx, placeFile.file);
                    places = null; // Force reload
                } catch (IOException e) {
                    Log.e(TAG, "Failed to fetch places", e);
                }
            } else {
                Log.i(TAG, "Places file is already fresh");
            }
        });
    }

    @Subscribe
    public void onSignIn(@NonNull AuthState.SignedIn event) {
        updateAsync(true);
    }

    @Subscribe
    public void onSignOut(@NonNull AuthState.SignedOut event) {
        if (placeFile != null) {
            placeFile.delete();
        }
        places = null;
        updateAsync(true);
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
        context = null;
    }

}
