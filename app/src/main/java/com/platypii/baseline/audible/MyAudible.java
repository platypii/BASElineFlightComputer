package com.platypii.baseline.audible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyLocationManager;

/**
 * Periodically gives audio feedback
 */
public class MyAudible {
    private static final String TAG = "Audible";

    private static Speech speech;
    private static AudibleThread audibleThread;
    private static SharedPreferences prefs;

    private static boolean isInitialized = false;

    public static void initAudible(Context context) {
        speech = new Speech(context);
        audibleThread = new AudibleThread();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean audibleEnabled = prefs.getBoolean("audible_enabled", false);
        if(audibleEnabled) {
            startAudible();
        }

        isInitialized = true;
    }

    public static void startAudible() {
        if(isInitialized) {
            audibleThread.start();
        } else {
            Log.e(TAG, "Failed to start audible: audible not initialized");
        }
    }

    public static void stopAudible() {
        if(isInitialized) {
            audibleThread.stop();
        } else {
            Log.e(TAG, "Failed to stop audible: audible not initialized");
        }
    }

    static void playAudio() {
        final String measurement = getMeasurement();
        if(measurement != null && measurement.length() > 0) {
            Log.i(TAG, "Saying " + measurement);
            speech.speakNow(measurement);
        } else {
            Log.i(TAG, "Saying nothing: no measurement");
        }
    }

    /**
     * Gets the speech rate from preferences
     * @return the delay between speech in milliseconds
     */
    static int getDelay() {
        final float speechRate = Float.parseFloat(prefs.getString("audible_rate", "2.0"));
        final int delay = (int) (speechRate * 1000f);
        return delay;
    }

    private static String getMeasurement() {
        final String audibleMode = prefs.getString("audible_mode", "horizontal_speed");
        final float min = Float.parseFloat(prefs.getString("audible_min", "60"));
        final float max = Float.parseFloat(prefs.getString("audible_max", "120"));
        String measurement = "";
        switch(audibleMode) {
            case "horizontal_speed":
                // Read horizontal speed
                final double horizontalSpeed = MyLocationManager.lastLoc.groundSpeed();
                if(isReal(horizontalSpeed) && min <= horizontalSpeed && horizontalSpeed <= max) {
                    measurement = String.format("%.0f", horizontalSpeed);
                } else {
                    Log.w(TAG, "Not speaking: horizontalSpeed " + horizontalSpeed);
                }
                break;
            case "vertical_speed":
                // Read vertical speed
                final double verticalSpeed = MyAltimeter.climb;
                if(isReal(verticalSpeed) && min <= verticalSpeed && verticalSpeed <= max) {
                    measurement = String.format("%.0f", verticalSpeed);
                } else {
                    Log.w(TAG, "Not speaking: verticalSpeed " + verticalSpeed);
                }
                break;
            case "glide_ratio":
                // Read glide ratio
                final double glideRatio = MyLocationManager.lastLoc.glideRatio();
                if(isReal(glideRatio) && min <= glideRatio && glideRatio <= max) {
                    measurement = String.format("%.1f", glideRatio);
                } else {
                    Log.w(TAG, "Not speaking: glideRatio " + glideRatio);
                }
                break;
            default:
                Log.e(TAG, "Invalid audible mode " + audibleMode);
        }
        return measurement;
    }

    private static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

}
