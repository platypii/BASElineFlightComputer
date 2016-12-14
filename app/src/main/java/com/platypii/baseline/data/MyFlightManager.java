package com.platypii.baseline.data;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.Services;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Util;

/**
 * A class to log, analyze, and provide feedback on flight data
 */
public class MyFlightManager {

    public static LatLng homeLoc;

    /**
     * Computes the estimated time to ground based on current altitude and climb
     */
    private static double timeToGround() {
        final double timeToGround = -Services.alti.altitudeAGL() / Services.alti.climb;
        if(!Util.isReal(timeToGround) || timeToGround < 0.01 || Math.abs(Services.alti.climb) < 0.05 || 24 * 60 * 60 < timeToGround) {
            // return NaN if we don't have an accurate landing location (climbing, very close to ground, very long estimate, etc)
            return Double.NaN;
        } else {
            return timeToGround;
        }
    }

    /**
     * Computes the estimated landing location based on current location and velocity
     */
    public static LatLng getLandingLocation() {
        // Compute time to ground
        final double timeToGround = timeToGround();
        if(Util.isReal(timeToGround) && Services.location.isFresh()) {

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

}
