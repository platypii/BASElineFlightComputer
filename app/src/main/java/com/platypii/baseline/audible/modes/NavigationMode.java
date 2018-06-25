package com.platypii.baseline.audible.modes;

import com.platypii.baseline.Services;
import com.platypii.baseline.audible.AudibleMode;
import com.platypii.baseline.audible.AudibleSample;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import android.support.annotation.NonNull;

/**
 * Navigation mode is intended to help navigate to a target destination
 * "1.0 miles, 30 (degrees) right"
 */
public class NavigationMode extends AudibleMode {

    // Have we spoken "stationary" yet?
    private boolean stationary = false;

    public NavigationMode() {
        super("navigation", "Navigation", "distance", 0, 6096, 2);
    }

    @Override
    public @NonNull
    AudibleSample currentSample(int precision) {
        double distance = 0.0;
        String measurement = "";
        final MLocation lastLoc = Services.location.lastLoc;
        if (LandingZone.homeLoc != null && lastLoc != null) {
            distance = lastLoc.distanceTo(LandingZone.homeLoc);
            if (lastLoc.groundSpeed() < 0.6) {
                // Only say stationary once
                if (!stationary) {
                    measurement = Convert.GLIDE_STATIONARY;
                }
                stationary = true;
            } else {
                stationary = false;
                final double homeBearing = lastLoc.bearingTo(LandingZone.homeLoc);
                final double deltaBearing = homeBearing - lastLoc.bearing();
                if (Math.abs(distance) > Convert.FT) {
                    measurement = Convert.distance2(distance, precision) + " " + Convert.angle2(deltaBearing);
                } else {
                    measurement = "0";
                }
            }
        }
        return new AudibleSample(distance, measurement);
    }

    @Override
    public float units() {
        return Convert.metric ? 1f : (float) Convert.FT;
    }

    @Override
    public String renderDisplay(double output, int precision) {
        return Convert.distance(output, precision, true);
    }
}
