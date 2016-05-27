package com.platypii.baseline.data;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.Services;
import com.platypii.baseline.data.measurements.MLocation;
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
        double timeToGround = -MyAltimeter.altitudeAGL() / MyAltimeter.climb;
        if(!Util.isReal(timeToGround) || timeToGround < 0.01 || Math.abs(MyAltimeter.climb) < 0.05 || 24 * 60 * 60 < timeToGround) {
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
        if(Util.isReal(timeToGround) && Services.location.lastLoc != null) {
            final MLocation currentLocation = Services.location.lastLoc;

            // Compute horizontal distance traveled at current velocity for timeToGround seconds
            double groundDistance = timeToGround * currentLocation.groundSpeed();
            double bearing = currentLocation.bearing();

            // Compute estimated landing location
            return currentLocation.moveDirection(bearing, groundDistance);
        } else {
            return null;
        }
    }

}
