package com.platypii.baseline.audible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.Services;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.data.MyFlightManager;
import com.platypii.baseline.util.Util;

/**
 * Periodically gives audio feedback
 */
public class MyAudible {
    private static final String TAG = "Audible";

    private Speech speech;
    private AudibleThread audibleThread;

    private boolean isInitialized = false;
    private boolean isEnabled = false;

    // Have we spoken "stationary" yet in glide ratio mode?
    private static boolean stationary = false;

    public void start(Context appContext) {
        Log.i(TAG, "Initializing audible");
        speech = new Speech(appContext);

        if(!isInitialized) {
            isInitialized = true;
            audibleThread = new AudibleThread(this);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            AudibleSettings.init(prefs);
            isEnabled = prefs.getBoolean("audible_enabled", false);
            if(isEnabled) {
                enableAudible();
            }
        } else {
            Log.w(TAG, "Audible initialized twice");
            FirebaseCrash.report(new IllegalStateException("Audible initialized twice"));
        }
    }

    public void enableAudible() {
        Log.i(TAG, "Starting audible");
        if(isInitialized) {
            if(!audibleThread.isRunning()) {
                audibleThread.start();

                // Say audible mode
                speakModeWhenReady();

                // Play first measurement
                speakWhenReady();
            } else {
                Log.w(TAG, "Audible thread already started");
            }
        } else {
            Log.e(TAG, "Failed to start audible: audible not initialized");
        }
        isEnabled = true;
    }

    public void disableAudible() {
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
    private void speakModeWhenReady() {
        speech.speakWhenReady(AudibleSettings.mode.name);
    }

    /**
     * Make a special announcement
     */
    public void speakNow(String text) {
        if(!isEnabled) {
            Log.e(TAG, "Should never speak when audible is disabled");
            FirebaseCrash.report(new IllegalStateException("MyAudible.speakNow should never speak when audible is disabled"));
        }
        if(speech != null) {
            speech.speakNow(text);
        } else {
            Log.e(TAG, "speakNow called but speech is null");
        }
    }

    void speak() {
        final String measurement = getMeasurement();
        if(speech != null && measurement != null && measurement.length() > 0) {
            speech.speakNow(measurement);
        }
    }

    private void speakWhenReady() {
        final String measurement = getMeasurement();
        if(speech != null && measurement != null && measurement.length() > 0) {
            speech.speakWhenReady(measurement);
        }
    }

    /**
     * Returns the text of what to say for the current measurement mode
     */
    private String getMeasurement() {
        String measurement = "";
        switch(AudibleSettings.mode.id) {
            case "total_speed":
                // Compute total speed
                if(goodGpsFix()) {
                    final double verticalSpeed = MyAltimeter.climb;
                    final double horizontalSpeed = Services.location.groundSpeed();
                    final double totalSpeed = Math.sqrt(verticalSpeed * verticalSpeed + horizontalSpeed * horizontalSpeed);
                    if (Util.isReal(totalSpeed) && AudibleSettings.min <= totalSpeed && totalSpeed <= AudibleSettings.max) {
                        measurement = shortSpeed(totalSpeed, AudibleSettings.precision);
                    } else {
                        Log.w(TAG, "Not speaking: total speed = " + Convert.speed(totalSpeed, AudibleSettings.precision, true));
                    }
                }
                break;
            case "horizontal_speed":
                // Read horizontal speed
                if(goodGpsFix()) {
                    final double horizontalSpeed = Services.location.groundSpeed();
                    if (Util.isReal(horizontalSpeed) && AudibleSettings.min <= horizontalSpeed && horizontalSpeed <= AudibleSettings.max) {
                        measurement = shortSpeed(horizontalSpeed, AudibleSettings.precision);
                    } else {
                        Log.w(TAG, "Not speaking: horizontal speed = " + Convert.speed(horizontalSpeed, AudibleSettings.precision, true));
                    }
                }
                break;
            case "vertical_speed":
                // Read vertical speed
                final double verticalSpeed = MyAltimeter.climb;
                if (Util.isReal(verticalSpeed) && AudibleSettings.min <= verticalSpeed && verticalSpeed <= AudibleSettings.max) {
                    if(verticalSpeed > 0) {
                        measurement = "+ " + shortSpeed(verticalSpeed, AudibleSettings.precision);
                    } else {
                        measurement = shortSpeed(-verticalSpeed, AudibleSettings.precision);
                    }
                } else {
                    Log.w(TAG, "Not speaking: vertical speed = " + Convert.speed(verticalSpeed, 0, true));
                }
                break;
            case "glide_ratio":
                // Read glide ratio
                if(goodGpsFix()) {
                    final double glideRatio = Services.location.lastLoc.glideRatio();
                    final String glideRatioString = Convert.glide(Services.location.groundSpeed(), MyAltimeter.climb, AudibleSettings.precision, false);
                    if(Util.isReal(glideRatio) && AudibleSettings.min <= glideRatio && glideRatio <= AudibleSettings.max) {
                        measurement = glideRatioString;
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
                        Log.w(TAG, "Not speaking: glide ratio = " + glideRatioString);
                    }
                }
                break;
            case "direction":
                if(goodGpsFix() && MyFlightManager.homeLoc != null) {
                    final double distance = Services.location.lastLoc.distanceTo(MyFlightManager.homeLoc);
                    if(AudibleSettings.min <= distance && distance <= AudibleSettings.max) {
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
                Log.e(TAG, "Invalid audible mode " + AudibleSettings.mode.id);
        }
        return measurement;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Return true if GPS signal is fresh
     */
    private boolean goodGpsFix() {
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
    private boolean gpsFix = false;

    /**
     * Generate the text to be spoken for speed.
     * Shortens 0.00 to 0
     */
    private String shortSpeed(double speed, int precision) {
        if(Math.abs(speed) < Math.pow(.1, precision) / 2) {
            return "0";
        } else {
            return Convert.speed(speed, precision, false);
        }
    }

    /**
     * Stop audible service
     */
    public void stop() {
        if(isInitialized) {
            if(audibleThread.isRunning()) {
                disableAudible();
            }
            audibleThread = null;
            isInitialized = false;
            speech = null;
        }
    }

}
