package com.platypii.baseline.places;

import com.platypii.baseline.location.Geo;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
            double bestDistance = Double.POSITIVE_INFINITY;
            // Find closest place
            for (Place place : placeList) {
                final double distance = Geo.fastDistance(loc.latitude, loc.longitude, place.lat, place.lng);
                if (distance < place.radius && distance < bestDistance) {
                    best = place;
                    bestDistance = distance;
                }
            }
            return best;
        } else {
            return null;
        }
    }

    @NonNull
    public String getString(@NonNull MLocation loc) {
        final Place place = cached(loc);
        if (place != null) {
            final double distance = Geo.distance(loc.latitude, loc.longitude, place.lat, place.lng);
            return String.format("%s (%s)", place, Convert.distance3(distance));
        } else {
            return "";
        }
    }

}
