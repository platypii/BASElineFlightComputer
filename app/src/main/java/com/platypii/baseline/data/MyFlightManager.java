package com.platypii.baseline.data;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.util.Util;

/**
 * A class to log, analyze, and provide feedback on flight data
 */
public class MyFlightManager {

    public static LatLng homeLoc;

    /**
     * Computes the estimated time to ground based on current location + velocity
     */
    public static double timeToGround() {
        double timeToGround = -MyAltimeter.altitude / MyAltimeter.climb;
        if(!Util.isReal(timeToGround) || timeToGround < 0.01 || Math.abs(MyAltimeter.climb) < 0.05 || 24 * 60 * 60 < timeToGround) {
            // return NaN if we don't have an accurate landing location (climbing, very close to ground, very long estimate, etc)
            return Double.NaN;
        } else {
            return timeToGround;
        }
    }

    /**
     * Computes the estimated landing location based on current location + velocity
     */
    public static LatLng getLandingLocation() {
        // Compute time to ground
        double timeToGround = timeToGround();
        if(Util.isReal(timeToGround)) {
            final MLocation loc = MyLocationManager.lastLoc;
            // Compute horizontal distance traveled at current velocity for timeToGround seconds
            double groundDistance = timeToGround * loc.groundSpeed();
            double bearing = loc.bearing();

            // Compute estimated landing location
            final MLocation currentLocation = MyLocationManager.lastLoc;
            final LatLng landingLocation = currentLocation.moveDirection(bearing, groundDistance);

            // Log.d("FlightManager", currentLocation + " -> " + landingLocation + " (" + groundDistance + "m, " + bearing + "°)");

            return landingLocation;
        } else {
            return null;
        }
    }

}