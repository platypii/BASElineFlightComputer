package com.platypii.baseline.jarvis;

import com.platypii.baseline.measurements.MLocation;

import androidx.annotation.NonNull;

/**
 * Determines the current flight mode
 * Attempts to detect: ground, plane, wingsuit, freefall, canopy, etc
 */
public class FlightMode {

    static final int MODE_UNKNOWN = 0;
    public static final int MODE_GROUND = 1;
    public static final int MODE_PLANE = 2;
    public static final int MODE_WINGSUIT = 3;
    public static final int MODE_FREEFALL = 4;
    public static final int MODE_CANOPY = 5;

    /**
     * Human readable mode strings
     */
    private static final String[] modeString = {
            "", "Ground", "Plane", "Wingsuit", "Freefall", "Canopy"
    };

    /**
     * Predict flight mode based on instantaneous horizontal and vertical velocity.
     */
    public static int getMode(@NonNull MLocation loc) {
        final double groundSpeed = loc.groundSpeed();
        final double climb = loc.climb;

        if (-0.3 * groundSpeed + 7 < climb && 33 < groundSpeed) {
            return MODE_PLANE;
        } else if (climb < -13 && climb < -groundSpeed - 10 && groundSpeed < 19) {
            return MODE_FREEFALL;
        } else if (climb < groundSpeed - 32 && climb < -0.3 * groundSpeed + 5.5) {
            return MODE_WINGSUIT;
        } else if (climb < -17) {
            return MODE_WINGSUIT;
        } else if (-11.5 < climb && climb < -1.1 && groundSpeed - 31 < climb && climb < groundSpeed - 4 && 1.1 < groundSpeed && groundSpeed < 23.5 && climb < -groundSpeed + 20) {
            return MODE_CANOPY;
        } else if (groundSpeed + Math.abs(climb - 1) < 5) {
            return MODE_GROUND;
        } else if (-1 < climb && climb < 2 && !(groundSpeed > 10)) {
            return MODE_GROUND;
        } else {
            return MODE_UNKNOWN;
        }
    }

    static String getModeString(int mode) {
        return modeString[mode];
    }

}
