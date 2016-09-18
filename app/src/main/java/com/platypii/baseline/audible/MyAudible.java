package com.platypii.baseline.audible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.platypii.baseline.Services;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.data.MyFlightManager;
import com.platypii.baseline.util.Util;

/**
 * Periodically gives audio feedback
 */
public class MyAudible {
    private static final String TAG = "Audible";

    private static Speech speech;
    private static AudibleThread audibleThread;
    private static SharedPreferences prefs;

    private static boolean isInitialized = false;
    private static boolean isEnabled = false;

    // Have we spoken "stationary" yet in glide ratio mode?
    private static boolean stationary = false;

    public static void init(Context appContext) {
        Log.i(TAG, "Initializing audible");
        speech = new Speech(appContext);

        if(!isInitialized) {
            isInitialized = true;
            audibleThread = new AudibleThread();

            prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            isEnabled = prefs.getBoolean("audible_enabled", false);
            if(isEnabled) {
                startAudible();
            }
        } else {
            Log.w(TAG, "Audible initialized twice");
        }
    }

    public static void startAudible() {
        Log.i(TAG, "Starting audible");
        if(isInitialized) {
            if(!audibleThread.isRunning()) {
                audibleThread.start();

                // Say audible mode
                speakModeWhenReady();

                // Play first measurement
                MyAudible.speakWhenReady();
            } else {
                Log.w(TAG, "Audible thread already started");
            }
        } else {
            Log.e(TAG, "Failed to start audible: audible not initialized");
        }
        isEnabled = true;
    }

    public static void stopAudible() {
        if(isInitialized) {
            speech.stopAll();
            audibleThread.stop();
            speech.speakWhenReady("Goodbye");
        } else {
            Log.e(TAG, "Failed to stop audible: audible not initialized");
        }
        isEnabled = false;
    }

    /**
     * Announce the current audible mode
     */
    private static void speakModeWhenReady() {
        final String audibleMode = prefs.getString("audible_mode", "");
        final AudibleMode mode = AudibleModes.get(audibleMode);
        speech.speakWhenReady(mode.name);
    }

    /**
     * Make a special announcement
     */
    public static void speakNow(String text) {
        if(!isEnabled) {
            Log.e(TAG, "Should never speak when audible is disabled");
        }
        speech.speakNow(text);
    }

    static void speak() {
        final String measurement = getMeasurement();
        if(measurement != null && measurement.length() > 0) {
            speech.speakNow(measurement);
        }
    }

    private static void speakWhenReady() {
        final String measurement = getMeasurement();
        if(measurement != null && measurement.length() > 0) {
            speech.speakWhenReady(measurement);
        }
    }

    /**
     * Gets the speech interval from preferences
     * @return the delay between speech in milliseconds
     */
    static int getInterval() {
        final float speechInterval = Float.parseFloat(prefs.getString("audible_interval", "2.5"));
        return (int) (speechInterval * 1000f);
    }

    /**
     * Gets the speech rate from preferences
     * @return the speech rate multiplier
     */
    static float getRate() {
        return Float.parseFloat(prefs.getString("audible_rate", "1.0"));
    }

    /**
     * Returns the text of what to say for the current measurement mode
     */
    private static String getMeasurement() {
        final String audibleMode = prefs.getString("audible_mode", "horizontal_speed");
        final double units = Convert.metric? Convert.KPH : Convert.MPH;
        final double min = Util.parseDouble(prefs.getString("audible_min", "-1000"));
        final double max = Util.parseDouble(prefs.getString("audible_max", "1000"));
        final int precision = Util.parseInt(prefs.getString("audible_precision", "1"), 1);
        String measurement = "";
        switch(audibleMode) {
            case "total_speed":
                // Compute total speed
                if(goodGpsFix()) {
                    final double verticalSpeed = MyAltimeter.climb;
                    final double horizontalSpeed = Services.location.lastLoc.groundSpeed();
                    final double totalSpeed = Math.sqrt(verticalSpeed * verticalSpeed + horizontalSpeed * horizontalSpeed);
                    if (Util.isReal(totalSpeed) && min * units <= totalSpeed && totalSpeed <= max * units) {
                        measurement = shortSpeed(totalSpeed, precision);
                    } else {
                        Log.w(TAG, "Not speaking: total speed = " + Convert.speed(totalSpeed, precision, true));
                    }
                }
                break;
            case "horizontal_speed":
                // Read horizontal speed
                if(goodGpsFix()) {
                    final double horizontalSpeed = Services.location.lastLoc.groundSpeed();
                    if (Util.isReal(horizontalSpeed) && min * units <= horizontalSpeed && horizontalSpeed <= max * units) {
                        measurement = shortSpeed(horizontalSpeed, precision);
                    } else {
                        Log.w(TAG, "Not speaking: horizontal speed = " + Convert.speed(horizontalSpeed, precision, true));
                    }
                }
                break;
            case "vertical_speed":
                // Read vertical speed
                final double verticalSpeed = MyAltimeter.climb;
                if (Util.isReal(verticalSpeed) && min * units <= verticalSpeed && verticalSpeed <= max * units) {
                    if(verticalSpeed > 0) {
                        measurement = "+ " + shortSpeed(verticalSpeed, precision);
                    } else {
                        measurement = shortSpeed(-verticalSpeed, precision);
                    }
                } else {
                    Log.w(TAG, "Not speaking: vertical speed = " + Convert.speed(verticalSpeed, 0, true));
                }
                break;
            case "glide_ratio":
                // Read glide ratio
                if(goodGpsFix()) {
                    final MLocation loc = Services.location.lastLoc;
                    final double glideRatio = loc.glideRatio();
                    if(Util.isReal(glideRatio) && min <= glideRatio && glideRatio <= max) {
                        measurement = loc.glideRatioString();
                        if(measurement.equals(Convert.GLIDE_STATIONARY)) {
                            if(stationary) {
                                // Only say stationary once
                                measurement = "";
                            }
                            stationary = true;
                        } else {
                            stationary = false;
                        }
                    } else {
                        Log.w(TAG, "Not speaking: glide ratio = " + loc.glideRatioString());
                    }
                }
                break;
            case "direction":
                if(goodGpsFix() && MyFlightManager.homeLoc != null) {
                    final double distance = Services.location.lastLoc.distanceTo(MyFlightManager.homeLoc);
                    if(min <= distance && distance <= max) {
                        final double bearing = Services.location.lastLoc.bearingTo(MyFlightManager.homeLoc);
                        if (Math.abs(distance) > 0.3) {
                            measurement = Convert.distance(distance) + " " + Convert.bearing(bearing);
                        } else {
                            measurement = "0";
                        }
                    }
                }
                break;
            default:
                Log.e(TAG, "Invalid audible mode " + audibleMode);
        }
        return measurement;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Return true if GPS signal is fresh
     */
    private static boolean goodGpsFix() {
        if(Services.location.lastLoc != null && Services.location.lastFixDuration() < 3500) {
            gpsFix = true;
        } else {
            if(Services.location.lastLoc == null) {
                Log.w(TAG, "No GPS signal");
            } else {
                Log.w(TAG, "Stale GPS signal");
            }
            if(gpsFix) {
                speech.speakNow("Signal lost");
            }
            gpsFix = false;
        }
        return gpsFix;
    }
    /** True iff the last measurement was a good fix */
    private static boolean gpsFix = false;

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

    /**
     * Stop audible service
     */
    public static void terminate() {
        if(isInitialized) {
            if(audibleThread.isRunning()) {
                stopAudible();
            }
            audibleThread = null;
            isInitialized = false;
            speech = null;
            prefs = null;
        }
    }

}
