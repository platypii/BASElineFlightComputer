package com.platypii.baseline.audible;

import com.platypii.baseline.util.Numbers;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

/**
 * Static class to store audible settings in memory.
 * Make sure to always update this if the underlying preference changes.
 */
public class AudibleSettings {

    // Defaults
    public boolean isEnabled = false;
    public boolean airplaneMode = true;
    @NonNull
    public AudibleMode mode = AudibleModes.get("horizontal_speed");
    public double min = mode.defaultMin;
    public double max = mode.defaultMax;
    public int precision = mode.defaultPrecision;
    public float speechInterval = 2.5f;
    public float speechRate = 1.0f;

    /**
     * Load audible settings from android preferences
     */
    public void load(@NonNull SharedPreferences prefs) {
        final String audibleMode = prefs.getString("audible_mode", "horizontal_speed");
        mode = AudibleModes.get(audibleMode);
        min = Numbers.parseDouble(prefs.getString("audible_min", Float.toString(mode.defaultMin)));
        max = Numbers.parseDouble(prefs.getString("audible_max", Float.toString(mode.defaultMax)));
        precision = Numbers.parseInt(prefs.getString("audible_precision", Integer.toString(mode.defaultPrecision)), mode.defaultPrecision);
        speechInterval = Numbers.parseFloat(prefs.getString("audible_interval", "2.5"));
        speechRate = Numbers.parseFloat(prefs.getString("audible_rate", "1.0"));
        isEnabled = prefs.getBoolean("audible_enabled", false);
        airplaneMode = prefs.getBoolean("audible_quiet", true);
    }

    /**
     * Change audible mode and set default min,max,precision values
     */
    public void setAudibleMode(@NonNull String audibleMode) {
        mode = AudibleModes.get(audibleMode);
        min = mode.defaultMin;
        max = mode.defaultMax;
        precision = mode.defaultPrecision;
    }
}
