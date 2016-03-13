package com.platypii.baseline.audible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.platypii.baseline.data.Convert;
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

    public static void init(Context appContext) {
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        speech = new Speech(appContext);

        if(!isInitialized) {
            isInitialized = true;
            audibleThread = new AudibleThread();
        } else {
            Log.w(TAG, "Audible initialized twice");
        }

        final boolean audibleEnabled = prefs.getBoolean("audible_enabled", false);
        // final boolean audibleEnabled = Boolean.parseBoolean(prefs.getString("audible_enabled", "false"));
        if(audibleEnabled) {
            startAudible();
        }
    }

    public static void startAudible() {
        if(isInitialized) {
            if(!audibleThread.isEnabled()) {
                audibleThread.start();

                // Say audible mode
                final String audibleMode = prefs.getString("audible_mode", "");
                speech.speakWhenReady(audibleMode.replace('_', ' '));

                // Play first measurement
                MyAudible.speakWhenReady();
            } else {
                Log.w(TAG, "Audible thread already started");
            }
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

    static void speak() {
        final String measurement = getMeasurement();
        if(measurement != null && measurement.length() > 0) {
            speech.speakNow(measurement);
        }
    }

    static void speakWhenReady() {
        final String measurement = getMeasurement();
        if(measurement != null && measurement.length() > 0) {
            speech.speakWhenReady(measurement);
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
                if(MyLocationManager.lastLoc != null) {
                    final double horizontalSpeed = Convert.mps2mph(MyLocationManager.lastLoc.groundSpeed());
                    if (isReal(horizontalSpeed) && min <= horizontalSpeed && horizontalSpeed <= max) {
                        measurement = String.format("%.0f", horizontalSpeed);
                    } else {
                        Log.w(TAG, "Not speaking: horizontal speed = " + horizontalSpeed);
                    }
                } else {
                    Log.w(TAG, "Not speaking: no gps signal received");
                }
                break;
            case "vertical_speed":
                // Read vertical speed
                final double verticalSpeed = Convert.mps2mph(MyAltimeter.climb);
                if(isReal(verticalSpeed) && min <= verticalSpeed && verticalSpeed <= max) {
                    measurement = String.format("%.0f", verticalSpeed);
                } else {
                    Log.w(TAG, "Not speaking: vertical speed = " + verticalSpeed);
                }
                break;
            case "glide_ratio":
                // Read glide ratio
                if(MyLocationManager.lastLoc != null) {
                    final double glideRatio = MyLocationManager.lastLoc.glideRatio();
                    if(isReal(glideRatio) && min <= glideRatio && glideRatio <= max) {
                        measurement = String.format("%.1f", glideRatio);
                    } else {
                        Log.w(TAG, "Not speaking: glide ratio = " + glideRatio);
                    }
                } else {
                    Log.w(TAG, "Not speaking: no gps signal received");
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

    /**
     * Stop audible service
     */
    public static void terminate() {
        if(isInitialized) {
            if(audibleThread.isEnabled()) {
                stopAudible();
            }
            audibleThread = null;
            isInitialized = false;
            speech = null;
            prefs = null;
        }
    }

}
