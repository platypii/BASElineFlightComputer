package com.platypii.baseline.audible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.platypii.baseline.util.Util;
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
        Log.i(TAG, "Initializing audible");
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        speech = new Speech(appContext);

        if(!isInitialized) {
            isInitialized = true;
            audibleThread = new AudibleThread();

            final boolean audibleEnabled = prefs.getBoolean("audible_enabled", false);
            if(audibleEnabled) {
                startAudible();
            }
        } else {
            Log.w(TAG, "Audible initialized twice");
        }
    }

    public static void startAudible() {
        Log.i(TAG, "Starting audible");
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

    public static boolean isEnabled() {
        return isInitialized && audibleThread != null && audibleThread.isEnabled();
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
        return (int) (speechRate * 1000f);
    }

    private static String getMeasurement() {
        final String audibleMode = prefs.getString("audible_mode", "horizontal_speed");
        final double units = Convert.metric? Convert.KPH : Convert.MPH;
        final double min = Util.parseDouble(prefs.getString("audible_min", "-1000"));
        final double max = Util.parseDouble(prefs.getString("audible_max", "1000"));
        String measurement = "";
        switch(audibleMode) {
            case "horizontal_speed":
                // Read horizontal speed
                if(MyLocationManager.lastLoc != null && MyLocationManager.lastFixDuration() < 5000) {
                    final double horizontalSpeed = MyLocationManager.lastLoc.groundSpeed();
                    if (Util.isReal(horizontalSpeed) && min * units <= horizontalSpeed && horizontalSpeed <= max * units) {
                        measurement = Convert.speed(horizontalSpeed, 0, false);
                    } else {
                        Log.w(TAG, "Not speaking: horizontal speed = " + Convert.speed(horizontalSpeed, 0, true));
                    }
                } else {
                    Log.w(TAG, "Not speaking: no gps signal");
                }
                break;
            case "vertical_speed":
                // Read vertical speed
                final double verticalSpeed = MyAltimeter.climb;
                if (Util.isReal(verticalSpeed) && min * units <= verticalSpeed && verticalSpeed <= max * units) {
                    if(verticalSpeed > 0) {
                        measurement = "+" + Convert.speed(verticalSpeed, 0, false);
                    } else {
                        measurement = Convert.speed(-verticalSpeed, 0, false);
                    }
                } else {
                    Log.w(TAG, "Not speaking: vertical speed = " + Convert.speed(verticalSpeed, 0, true));
                }
                break;
            case "glide_ratio":
                // Read glide ratio
                if(MyLocationManager.lastLoc != null && MyLocationManager.lastFixDuration() < 5000) {
                    final double glideRatio = MyLocationManager.lastLoc.glideRatio();
                    if(Util.isReal(glideRatio) && min <= glideRatio && glideRatio <= max) {
                        measurement = Convert.glide(glideRatio, 1, false);
                    } else {
                        Log.w(TAG, "Not speaking: glide ratio = " + Convert.glide(glideRatio, 1, true));
                    }
                } else {
                    Log.w(TAG, "Not speaking: no gps signal");
                }
                break;
//            case "position":
//                // Read position N10E45
//                if(MyLocationManager.lastLoc != null && MyLocationManager.lastFixDuration() < 5000) {
//                    if(MyFlightManager.homeLoc != null) {
//                        final double bearing = MyLocationManager.lastLoc.bearingTo(MyFlightManager.homeLoc);
//                        if(-270 < bearing || bearing < 90) {
//                            // North
//                        } else {
//                            // South
//                        }
//                        if(bearing < 180) {
//                            // East
//                        } else {
//                            // West
//                        }
//                    }
//                } else {
//                    Log.w(TAG, "Not speaking: no gps signal");
//                }
            default:
                Log.e(TAG, "Invalid audible mode " + audibleMode);
        }
        return measurement;
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
