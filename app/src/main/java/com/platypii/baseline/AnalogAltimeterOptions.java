package com.platypii.baseline;

import com.platypii.baseline.util.Convert;

class AnalogAltimeterOptions {

    // Fixed altitudes
    final double max_altitude;

    // Precomputed angles for drawing
    final float breakoff_angle;
    final float deploy_angle;
    final float harddeck_angle;
    final float major_angle;

    private AnalogAltimeterOptions(
            double max_altitude,
            double breakoff_altitude,
            double deploy_altitude,
            double harddeck_altitude,
            double major_units
    ) {
        this.max_altitude = max_altitude;
        // Precompute angle for drawing
        breakoff_angle = (float) (360 * breakoff_altitude / max_altitude);
        deploy_angle   = (float) (360 * deploy_altitude / max_altitude);
        harddeck_angle = (float) (360 * harddeck_altitude / max_altitude);
        major_angle = (float) (2.0 * Math.PI * major_units / max_altitude);
    }

    static final AnalogAltimeterOptions imperial = new AnalogAltimeterOptions(
            12000 * Convert.FT,
            4000 * Convert.FT,
            3000 * Convert.FT,
            2000 * Convert.FT,
            1000 * Convert.FT
    );
    static final AnalogAltimeterOptions metric = new AnalogAltimeterOptions(
            4000,
            1333,
            1000,
            666,
            1000
    );

}
