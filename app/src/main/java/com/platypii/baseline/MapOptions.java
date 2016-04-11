package com.platypii.baseline;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.util.Util;

/**
 * Contains configuration settings for navigation mode
 */
class MapOptions {

    // Default map center, until we get gps data
    static final LatLng defaultLatLng = new LatLng(47.239, -123.143); // kpow
    // usa: LatLng(41.2, -120.5)
    static final float defaultZoom = 6;

    // Time from last drag to snap back to user location
    static final long SNAP_BACK_TIME = 5000; // millis

    // Maximum camera animation duration
    static final int MAX_ANIMATION_DURATION = 900; // millis

    /**
     * Returns the default zoom for a given altitude
     */
    static float getZoom() {
        final double altitude = MyAltimeter.altitudeAGL();

        // Piecewise linear zoom function
        final double alts[] = {100, 600, 1200, 2000};
        final float zooms[] = {17f, 13.5f, 12.8f, 11.8f};

        // Find which linear segment altitude lies in
        // when loop completes, alts[index] <= altitude < alts[index+1]
        int index = -1;
        while(index < alts.length - 1 && alts[index+1] <= altitude) {
            index++;
        }

        if(index == -1) {
            return zooms[0];
        } else if(index < alts.length - 1) {
            // Linear interpolation
            return zooms[index+1] - (float) ((alts[index+1] - altitude) * (zooms[index+1] - zooms[index]) / (alts[index+1] - alts[index]));
        } else {
            return zooms[alts.length - 1];
        }
    }

    /**
     * Determine the zoom animation duration based on GPS refresh rate and MAX_DURATION
     * @return the zoom duration in milliseconds
     */
    static int zoomDuration() {
        if(Util.isReal(Services.location.refreshRate) && Services.location.refreshRate > 0) {
            final int gpsUpdateDuration = (int) (1000f / Services.location.refreshRate);
            return Math.min(gpsUpdateDuration, MAX_ANIMATION_DURATION);
        } else {
            return MAX_ANIMATION_DURATION;
        }
    }

}
