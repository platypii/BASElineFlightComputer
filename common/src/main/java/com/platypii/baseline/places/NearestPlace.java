package com.platypii.baseline.places;

import com.platypii.baseline.location.Geo;
import com.platypii.baseline.measurements.MLocation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

/**
 * Nearest place cache, since it doesn't change that often
 */
public class NearestPlace {

    // Update once per minute
    private static final long ttl = 60000; // milliseconds

    @NonNull
    private final Places places;

    @Nullable
    private Place lastPlace = null;
    private long lastQuery = 0;

    NearestPlace(@NonNull Places places) {
        this.places = places;
    }

    @Nullable
    public Place cached(@NonNull MLocation current) {
        if (System.currentTimeMillis() - lastQuery > ttl) {
            lastQuery = System.currentTimeMillis();
            lastPlace = get(current);
            return lastPlace;
        } else {
            return lastPlace;
        }
    }

    /**
     * Find the closest place to the given location
     */
    @Nullable
    private Place get(@NonNull MLocation loc) {
        final List<Place> placeList = places.getPlaces();
        if (placeList != null) {
            Place best = null;
            double bestDistance = Double.NaN;
            // Find closest place
            for (Place place : placeList) {
                final double distance = Geo.fastDistance(loc.latitude, loc.longitude, place.latitude, place.longitude);
                if (Double.isNaN(bestDistance) || (distance < bestDistance && distance < place.radius)) {
                    best = place;
                    bestDistance = distance;
                }
            }
            return best;
        } else {
            return null;
        }
    }

}
