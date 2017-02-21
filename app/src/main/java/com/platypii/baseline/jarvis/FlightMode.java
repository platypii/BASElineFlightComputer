package com.platypii.baseline.jarvis;

import com.platypii.baseline.Services;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;

/**
 * Determines the current flight mode
 * Attempts to detect: ground, plane, freefall, canopy
 */
public class FlightMode implements MyLocationListener {

    private static final int MODE_UNKNOWN = 0;
    private static final int MODE_GROUND = 1;
    private static final int MODE_CLIMB = 2;
    private static final int MODE_FREEFALL = 3;
    private static final int MODE_CANOPY = 4;

    /**
     * Human readable mode strings
     */
    private static final String[] modeString = {
            "", "Ground","Climb","Freefall","Canopy"
    };

    private int flightMode = MODE_UNKNOWN;

    public void start() {
        // Start listening for location updates
        Services.location.addListener(this);
    }

    @Override
    public void onLocationChanged(MLocation loc) {
        final double groundSpeed = loc.groundSpeed();
        final double climb = loc.climb;
        flightMode = getMode(groundSpeed, climb);
    }
    @Override
    public void onLocationChangedPostExecute() {}

    /**
     * Heuristic to determine flight mode based on horizontal and vertical velocity
     * TODO: Optimize parameters
     * TODO: Use machine learning model
     */
    private static int getMode(double groundSpeed, double climb) {
        if(-5 < climb && 35 < groundSpeed) {
            // Speed at least 80mph
            return MODE_CLIMB;
        } else if(climb < -10 && 32 < groundSpeed) {
            // Falling at least 20 mph, speed at least 50 mph
            return MODE_FREEFALL; // Wingsuit
        } else if(climb < -13) {
            // Falling at least 30 mph
            return MODE_FREEFALL;
        } else if(-9 < climb && climb < -1.1 && groundSpeed < 25) {
            // Descending at least 2-20mph, speed less than 55mph
            return MODE_CANOPY;
        } else if(-1.1 < climb && climb < 1.1 && groundSpeed < 32) {
            // Ground speed no greater than 50 mph
            return MODE_GROUND;
        } else {
            return MODE_UNKNOWN;
        }
    }

    public String getModeString() {
        return modeString[flightMode];
    }

    public void stop() {
        // Stop location updates
        Services.location.removeListener(this);
    }

}
