package com.platypii.baseline.audible.modes;

import com.platypii.baseline.Services;
import com.platypii.baseline.audible.AudibleSample;
import com.platypii.baseline.util.Convert;
import androidx.annotation.NonNull;

public class TotalSpeedMode extends SpeedMode {

    public TotalSpeedMode() {
        super("total_speed", "Total Speed", 0, 200 * Convert.MPHf);
    }

    @Override
    public @NonNull AudibleSample currentSample(int precision) {
        final double totalSpeed = Services.location.totalSpeed();
        return new AudibleSample(totalSpeed, shortSpeed(totalSpeed, precision));
    }
}
