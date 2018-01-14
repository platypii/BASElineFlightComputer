package com.platypii.baseline.location;

import android.support.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Stores the target landing zone to be displayed on the map.
 * Also computes projected landing zone based on current position, velocity and altitude AGL.
 */
public class LandingZone {

    /**
     * Home location is used as the target on the map
     */
    @Nullable
    public static LatLng homeLoc;

}
