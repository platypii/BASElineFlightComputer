package com.platypii.baseline.audible.modes;

import com.platypii.baseline.Services;
import com.platypii.baseline.audible.AudibleSample;
import com.platypii.baseline.util.Convert;
import android.support.annotation.NonNull;

public class VerticalSpeedMode extends SpeedMode {

    public VerticalSpeedMode() {
        super("vertical_speed", "Vertical Speed", -140 * Convert.MPHf, 0);
    }

    @Override
    public @NonNull AudibleSample currentSample(int precision) {
        final double verticalSpeed = Services.alti.climb;
        final String verticalSpeedString;
        if (verticalSpeed > 0) {
            verticalSpeedString = "+ " + shortSpeed(verticalSpeed, precision);
        } else {
            verticalSpeedString = shortSpeed(-verticalSpeed, precision);
        }
        return new AudibleSample(verticalSpeed, verticalSpeedString);
    }
}
