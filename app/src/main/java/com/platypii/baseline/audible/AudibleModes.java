package com.platypii.baseline.audible;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.Services;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.util.Convert;

/**
 * Contains the default audible modes
 */
class AudibleModes {
    private static final String TAG = "AudibleMode";

    public static AudibleMode get(String audibleMode) {
        switch(audibleMode) {
            case "horizontal_speed":
                return horizontal_speed;
            case "vertical_speed":
                return vertical_speed;
            case "total_speed":
                return total_speed;
            case "glide_ratio":
                return glide_ratio;
            default:
                Log.e(TAG, "Invalid audible mode " + audibleMode);
                FirebaseCrash.report(new IllegalStateException("Invalid audible mode " + audibleMode));
                return horizontal_speed;
        }
    }

    private static final AudibleMode horizontal_speed = new AudibleMode("horizontal_speed", "Horizontal Speed", "speed", 0, 180 * Convert.MPHf, 0) {
        @Override
        public @NonNull AudibleSample currentSample(int precision) {
            final double horizontalSpeed = Services.location.groundSpeed();
            return new AudibleSample(horizontalSpeed, shortSpeed(horizontalSpeed, precision));
        }
        @Override
        public float units() {
            return Convert.metric? Convert.KPHf : Convert.MPHf;
        }
        @Override
        public String renderDisplay(double output, int precision) {
            return Convert.speed(output, precision, true);
        }
    };

    private static final AudibleMode vertical_speed = new AudibleMode("vertical_speed", "Vertical Speed", "speed", -140 * Convert.MPHf, 0, 0) {
        @Override
        public @NonNull AudibleSample currentSample(int precision) {
            final double verticalSpeed = MyAltimeter.climb;
            final String verticalSpeedString;
            if (verticalSpeed > 0) {
                verticalSpeedString = "+ " + shortSpeed(verticalSpeed, precision);
            } else {
                verticalSpeedString = shortSpeed(-verticalSpeed, precision);
            }
            return new AudibleSample(verticalSpeed, verticalSpeedString);
        }
        @Override
        public float units() {
            return Convert.metric? Convert.KPHf : Convert.MPHf;
        }
        @Override
        public String renderDisplay(double output, int precision) {
            return Convert.speed(output, precision, true);
        }
    };

    private static final AudibleMode total_speed = new AudibleMode("total_speed", "Total Speed", "speed", 0, 200 * Convert.MPHf, 0) {
        @Override
        public @NonNull AudibleSample currentSample(int precision) {
            final double verticalSpeed = MyAltimeter.climb;
            final double horizontalSpeed = Services.location.groundSpeed();
            final double totalSpeed = Math.sqrt(verticalSpeed * verticalSpeed + horizontalSpeed * horizontalSpeed);
            return new AudibleSample(totalSpeed, shortSpeed(totalSpeed, precision));
        }
        @Override
        public float units() {
            return Convert.metric? Convert.KPHf : Convert.MPHf;
        }
        @Override
        public String renderDisplay(double output, int precision) {
            return Convert.speed(output, precision, true);
        }
    };

    private static final AudibleMode glide_ratio = new AudibleMode("glide_ratio", "Glide Ratio", "glide ratio", 0, 4, 1) {
        // Have we spoken "stationary" yet?
        private boolean stationary = false;

        @Override
        public @NonNull AudibleSample currentSample(int precision) {
            final double glideRatio = Services.location.glideRatio();
            String glideRatioString = Convert.glide(Services.location.groundSpeed(), MyAltimeter.climb, AudibleSettings.precision, false);
            if(glideRatioString.equals(Convert.GLIDE_STATIONARY)) {
                if(stationary) {
                    // Only say stationary once
                    glideRatioString = "";
                }
                stationary = true;
            } else {
                stationary = false;
            }
            return new AudibleSample(glideRatio, glideRatioString);
        }
        @Override
        public float units() {
            return 1;
        }
        @Override
        public String renderDisplay(double output, int precision) {
            return Convert.glide(output, precision, true);
        }
    };

//    private static final AudibleMode distance = new AudibleMode("distance", "Distance", "distance", 0, 1000, 0) {
//        @Override
//        public @NonNull AudibleSample currentSample(int precision) {
//            double distance = 0.0;
//            String distanceString = "";
//            if(MyFlightManager.homeLoc != null) {
//                distance = Services.location.lastLoc.distanceTo(MyFlightManager.homeLoc);
//                if(AudibleSettings.min <= distance && distance <= AudibleSettings.max) {
//                    final double bearing = Services.location.lastLoc.bearingTo(MyFlightManager.homeLoc);
//                    if (Math.abs(distance) > 0.3) {
//                        distanceString = Convert.distance(distance) + " " + Convert.bearing(bearing);
//                    } else {
//                        distanceString = "0";
//                    }
//                }
//            }
//            return new AudibleSample(distance, distanceString);
//        }
//        @Override
//        public float units() {
//            return Convert.metric? 1f : (float) Convert.FT;
//        }
//        @Override
//        public String renderDisplay(double output, int precision) {
//            return Convert.distance(output, precision, true);
//        }
//    };

    /**
     * Generate the text to be spoken for speed.
     * Shortens 0.00 to 0
     */
    private static String shortSpeed(double speed, int precision) {
        if(Math.abs(speed) < Math.pow(.1, precision) / 2) {
            return "0";
        } else {
            return Convert.speed(speed, precision, false);
        }
    }

}
