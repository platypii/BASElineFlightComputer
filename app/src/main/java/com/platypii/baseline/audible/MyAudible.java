package com.platypii.baseline.audible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
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

    private static Speech speech;
    private static AudibleThread audibleThread;
    private static SharedPreferences prefs;

    private static boolean isInitialized = false;
    private static boolean isEnabled = false;

    private static boolean northsouth = true;

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
        isEnabled = true;
    }

    public static void stopAudible() {
        if(isInitialized) {
            audibleThread.stop();
            speech.speakWhenReady("Goodbye");
        } else {
            Log.e(TAG, "Failed to stop audible: audible not initialized");
        }
        isEnabled = false;
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
     * Gets the speech rate from preferences
     * @return the delay between speech in milliseconds
     */
    static int getDelay() {
        final float speechInterval = Float.parseFloat(prefs.getString("audible_interval", "2.0"));
        return (int) (speechInterval * 1000f);
    }

    private static String getMeasurement() {
        final String audibleMode = prefs.getString("audible_mode", "horizontal_speed");
        final double units = Convert.metric? Convert.KPH : Convert.MPH;
        final double min = Util.parseDouble(prefs.getString("audible_min", "-1000"));
        final double max = Util.parseDouble(prefs.getString("audible_max", "1000"));
        String measurement = "";
        switch(audibleMode) {
            // TODO: True Air Speed
            case "total_speed":
                // Compute total speed
                if(Services.location.lastLoc != null && Services.location.lastFixDuration() < 5000) {
                    final double verticalSpeed = MyAltimeter.climb;
                    final double horizontalSpeed = Services.location.lastLoc.groundSpeed();
                    final double totalSpeed = Math.sqrt(verticalSpeed * verticalSpeed + horizontalSpeed * horizontalSpeed);
                    if (Util.isReal(totalSpeed) && min * units <= totalSpeed && totalSpeed <= max * units) {
                        measurement = Convert.speed(totalSpeed, 0, false);
                    } else {
                        Log.w(TAG, "Not speaking: total speed = " + Convert.speed(totalSpeed, 0, true));
                    }
                } else {
                    Log.w(TAG, "Not speaking: no gps signal");
                }
                break;
            case "horizontal_speed":
                // Read horizontal speed
                if(Services.location.lastLoc != null && Services.location.lastFixDuration() < 5000) {
                    final double horizontalSpeed = Services.location.lastLoc.groundSpeed();
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
                if(Services.location.lastLoc != null && Services.location.lastFixDuration() < 5000) {
                    final double glideRatio = Services.location.lastLoc.glideRatio();
                    if(Util.isReal(glideRatio) && min <= glideRatio && glideRatio <= max) {
                        measurement = Convert.glide(glideRatio, 1, false);
                    } else {
                        Log.w(TAG, "Not speaking: glide ratio = " + Convert.glide(glideRatio, 1, true));
                    }
                } else {
                    Log.w(TAG, "Not speaking: no gps signal");
                }
                break;
            case "direction":
                if(Services.location.lastLoc != null && Services.location.lastFixDuration() < 5000) {
                    if(MyFlightManager.homeLoc != null) {
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
                } else {
                    Log.w(TAG, "Not speaking: no gps signal");
                }
                break;
            case "position":
                // Read position N10E45
                if(Services.location.lastLoc != null && Services.location.lastFixDuration() < 5000) {
                    if(MyFlightManager.homeLoc != null) {
                        final double bearing = Services.location.lastLoc.bearingTo(MyFlightManager.homeLoc);
                        final double distance = Services.location.lastLoc.distanceTo(MyFlightManager.homeLoc);
                        final double distance_x = distance * Math.sin(Math.toRadians(bearing));
                        final double distance_y = distance * Math.cos(Math.toRadians(bearing));
                        if(northsouth && Math.abs(distance_y) > 0.3) {
                            if (-90 < bearing && bearing < 90) {
                                measurement = "north " + Convert.distance(distance_y, 0, false);
                            } else {
                                measurement = "south " + Convert.distance(distance_y, 0, false);
                            }
                            northsouth = false;
                        } else if(Math.abs(distance_x) > 0.3) {
                            if(0 < bearing) {
                                measurement += "east " + Convert.distance(distance_x, 0, false);
                            } else {
                                measurement += "west " + Convert.distance(distance_x, 0, false);
                            }
                            northsouth = true;
                        } else {
                            measurement = "0";
                        }
                    }
                } else {
                    Log.w(TAG, "Not speaking: no gps signal");
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

//    public static boolean isRunning() {
//        return isInitialized && audibleThread != null && audibleThread.isRunning();
//    }

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
