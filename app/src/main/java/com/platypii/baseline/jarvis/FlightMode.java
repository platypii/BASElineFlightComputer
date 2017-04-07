package com.platypii.baseline.jarvis;

/**
 * Determines the current flight mode
 * Attempts to detect: ground, plane, wingsuit, freefall, canopy, etc
 */
public class FlightMode {

    static final int MODE_UNKNOWN = 0;
    static final int MODE_GROUND = 1;
    private static final int MODE_PLANE = 2;
    private static final int MODE_WINGSUIT = 3;
    private static final int MODE_FREEFALL = 4;
    static final int MODE_CANOPY = 5;

    /**
     * Human readable mode strings
     */
    private static final String[] modeString = {
            "", "Ground","Plane","Wingsuit","Freefall","Canopy"
    };

    /**
     * Heuristic to determine flight mode based on horizontal and vertical velocity
     * TODO: Optimize parameters
     * TODO: Use machine learning model
     */
    public static int getMode(double groundSpeed, double climb) {
        if(-5 < climb && 35 < groundSpeed) {
            // Speed at least 80mph
            return MODE_PLANE;
        } else if(climb < -10 && 32 < groundSpeed) {
            // Falling at least 20 mph, speed at least 50 mph
            return MODE_WINGSUIT;
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

    static String getModeString(int mode) {
        return modeString[mode];
    }

}
