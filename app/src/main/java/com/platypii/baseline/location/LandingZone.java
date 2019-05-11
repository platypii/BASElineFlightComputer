package com.platypii.baseline.location;

import com.platypii.baseline.Services;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
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

    /**
     * Computes the estimated landing location based on current location and velocity
     */
    public static LatLng getLandingLocation() {
        // Compute time to ground
        final double timeToGround = timeToGround();
        if (Numbers.isReal(timeToGround) && Services.location.isFresh()) {

            // Compute horizontal distance traveled at current velocity for timeToGround seconds
            final double groundDistance = timeToGround * Services.location.groundSpeed();
            final double bearing = Services.location.bearing();

            // Compute estimated landing location
            final MLocation currentLocation = Services.location.lastLoc;
            return currentLocation.moveDirection(bearing, groundDistance);
        } else {
            return null;
        }
    }

    /**
     * Computes the estimated time to ground based on current altitude and climb
     */
    private static double timeToGround() {
        final double timeToGround = -Services.alti.altitudeAGL() / Services.alti.climb;
        if (!Numbers.isReal(timeToGround) || timeToGround < 0.01 || Math.abs(Services.alti.climb) < 0.05 || 24 * 60 * 60 < timeToGround) {
            // return NaN if we don't have an accurate landing location (climbing, very close to ground, very long estimate, etc)
            return Double.NaN;
        } else {
            return timeToGround;
        }
    }

    public static void setHomeLocation(Context context, @Nullable LatLng home) {
        LandingZone.homeLoc = home;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        if (home != null) {
            editor.putString("home_latitude", Double.toString(home.latitude));
            editor.putString("home_longitude", Double.toString(home.longitude));
        } else {
            editor.putString("home_latitude", null);
            editor.putString("home_longitude", null);
        }
        editor.apply();
    }

}
