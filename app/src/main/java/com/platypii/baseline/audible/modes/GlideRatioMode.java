package com.platypii.baseline.audible.modes;

import com.platypii.baseline.Services;
import com.platypii.baseline.audible.AudibleMode;
import com.platypii.baseline.audible.AudibleSample;
import com.platypii.baseline.audible.AudibleSettings;
import com.platypii.baseline.util.Convert;
import android.support.annotation.NonNull;

public class GlideRatioMode extends AudibleMode {

    // Have we spoken "stationary" yet?
    private boolean stationary = false;

    public GlideRatioMode() {
        super("glide_ratio", "Glide Ratio", "glide ratio", 0, 4, 1);
    }

    @Override
    public @NonNull
    AudibleSample currentSample(int precision) {
        final double glideRatio = Services.location.glideRatio();
        String glideRatioString = Convert.glide(Services.location.groundSpeed(), Services.alti.climb, AudibleSettings.precision, false);
        if(glideRatioString.equals(Convert.GLIDE_STATIONARY)) {
            if(stationary) {
                // Only say stationary once
                glideRatioString = "";
            }
            stationary = true;
        } else {
            stationary = false;
        }
        return new AudibleSample(glideRatio, glideRatioString);
    }

    @Override
    public float units() {
        return 1;
    }

    @Override
    public String renderDisplay(double output, int precision) {
        return Convert.glide(output, precision, true);
    }
}
