package com.platypii.baseline.audible.modes;

import com.platypii.baseline.Services;
import com.platypii.baseline.audible.AudibleSample;
import com.platypii.baseline.util.Convert;
import android.support.annotation.NonNull;

public class HorizontalSpeedMode extends SpeedMode {

    public HorizontalSpeedMode() {
        super("horizontal_speed", "Horizontal Speed", 0, 180 * Convert.MPHf);
    }

    @Override
    public @NonNull
    AudibleSample currentSample(int precision) {
        final double horizontalSpeed = Services.location.groundSpeed();
        return new AudibleSample(horizontalSpeed, shortSpeed(horizontalSpeed, precision));
    }
}
