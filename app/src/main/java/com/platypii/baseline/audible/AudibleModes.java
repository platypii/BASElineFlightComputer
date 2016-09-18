package com.platypii.baseline.audible;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.util.Convert;

/**
 * Contains the default audible modes
 */
public class AudibleModes {
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
                return null;
        }
    }

    public static AudibleMode horizontal_speed = new AudibleMode("horizontal_speed", "Horizontal Speed", "speed", 0, 180 * Convert.MPHf) {
        @Override
        public float units() {
            return Convert.metric? Convert.KPHf : Convert.MPHf;
        }
        @Override
        public String convertOutput(double output) {
            return Convert.speed(output, 0, true);
        }
    };

    public static AudibleMode vertical_speed = new AudibleMode("vertical_speed", "Vertical Speed", "speed", -140 * Convert.MPHf, 0) {
        @Override
        public float units() {
            return Convert.metric? Convert.KPHf : Convert.MPHf;
        }
        @Override
        public String convertOutput(double output) {
            return Convert.speed(output, 0, true);
        }
    };

    public static AudibleMode total_speed = new AudibleMode("total_speed", "Total Speed", "speed", 0, 200 * Convert.MPHf) {
        @Override
        public float units() {
            return Convert.metric? Convert.KPHf : Convert.MPHf;
        }
        @Override
        public String convertOutput(double output) {
            return Convert.speed(output, 0, true);
        }
    };

    public static AudibleMode glide_ratio = new AudibleMode("glide_ratio", "Glide Ratio", "glide ratio", 0, 4) {
        @Override
        public float units() {
            return 1;
        }
        @Override
        public String convertOutput(double output) {
            return Convert.glide(output, 0, true);
        }
    };

}
