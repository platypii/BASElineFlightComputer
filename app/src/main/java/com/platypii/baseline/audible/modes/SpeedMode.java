package com.platypii.baseline.audible.modes;

import com.platypii.baseline.audible.AudibleMode;
import com.platypii.baseline.util.Convert;

import androidx.annotation.NonNull;

abstract class SpeedMode extends AudibleMode {

    SpeedMode(String id, String name, float defaultMin, float defaultMax) {
        super(id, name, "speed", defaultMin, defaultMax, 0);
    }

    @Override
    public float units() {
        return Convert.metric ? Convert.KPHf : Convert.MPHf;
    }

    @NonNull
    @Override
    public String renderDisplay(double output, int precision) {
        return Convert.speed(output, precision, true);
    }

    /**
     * Generate the text to be spoken for speed.
     * Shortens 0.00 to 0
     */
    @NonNull
    static String shortSpeed(double speed, int precision) {
        if (Math.abs(speed) < Math.pow(.1, precision) / 2) {
            return "0";
        } else {
            return Convert.speed(speed, precision, false);
        }
    }

}
