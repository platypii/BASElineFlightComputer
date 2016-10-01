package com.platypii.baseline.audible;

import android.content.SharedPreferences;
import com.platypii.baseline.util.Util;

/**
 * Static class to store audible settings in memory.
 * Make sure to always update this if the underlying preference changes.
 */
public class AudibleSettings {

    static String audibleMode;
    public static AudibleMode mode;
    public static double min;
    public static double max;
    public static int precision;
    public static float speechInterval;
    public static float speechRate;

    /**
     * Load audible settings from android preferences
     */
    static void init(SharedPreferences prefs) {
        audibleMode = prefs.getString("audible_mode", "horizontal_speed");
        mode = AudibleModes.get(audibleMode);
        min = Util.parseDouble(prefs.getString("audible_min", Float.toString(mode.defaultMin)));
        max = Util.parseDouble(prefs.getString("audible_max", Float.toString(mode.defaultMax)));
        precision = Util.parseInt(prefs.getString("audible_precision", Integer.toString(mode.defaultPrecision)), mode.defaultPrecision);
        speechInterval = Float.parseFloat(prefs.getString("audible_interval", "2.5"));
        speechRate = Float.parseFloat(prefs.getString("audible_rate", "1.0"));
    }

    /**
     * Change audible mode and set default min,max,precision values
     */
    public static void setAudibleMode(String newMode) {
        audibleMode = newMode;
        mode = AudibleModes.get(audibleMode);
        min = mode.defaultMin;
        max = mode.defaultMax;
        precision = mode.defaultPrecision;
    }
}
