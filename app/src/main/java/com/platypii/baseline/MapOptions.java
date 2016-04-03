package com.platypii.baseline;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.util.Util;

/**
 * Contains configuration settings for navigation mode
 */
class MapOptions {

    // Default map center, until we get gps data
    static final LatLng defaultLatLng = new LatLng(47.239, -123.143); // kpow
    // usa: LatLng(41.2, -120.5)

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
        final double alts[] = {100, 600, 2000};
        final float zooms[] = {17.9f, 14f, 12.5f};

        if(altitude < alts[0]) {
            return zooms[0];
        } else if(altitude <= alts[1]) {
            // Linear interpolation
            return zooms[1] - (float) ((alts[1] - altitude) * (zooms[1] - zooms[0]) / (alts[1] - alts[0]));
        } else if(altitude <= alts[2]) {
            // Linear interpolation
            return zooms[2] - (float) ((alts[2] - altitude) * (zooms[2] - zooms[1]) / (alts[2] - alts[1]));
        } else {
            return zooms[2];
        }
    }

    /**
     * Determine the zoom animation duration based on GPS refresh rate and MAX_DURATION
     * @return the zoom duration in milliseconds
     */
    static int zoomDuration() {
        if(Util.isReal(MyLocationManager.refreshRate) && MyLocationManager.refreshRate > 0) {
            final int gpsUpdateDuration = (int) (1000f / MyLocationManager.refreshRate);
            return Math.min(gpsUpdateDuration, MAX_ANIMATION_DURATION);
        } else {
            return MAX_ANIMATION_DURATION;
        }
    }

}
