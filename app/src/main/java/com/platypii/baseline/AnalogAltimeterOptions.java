package com.platypii.baseline;

import com.platypii.baseline.util.Convert;

public class AnalogAltimeterOptions {

    // Fixed altitudes
    public final double max_altitude;
    public final double breakoff_altitude;
    public final double deploy_altitude;
    public final double harddeck_altitude;

    // Units
    public final double major_units;
    public final String units_label;

    // Precomputed angles for drawing
    public final float breakoff_angle;
    public final float deploy_angle;
    public final float harddeck_angle;
    public final float major_angle;

    public AnalogAltimeterOptions(
            double max_altitude,
            double breakoff_altitude,
            double deploy_altitude,
            double harddeck_altitude,
            double major_units,
            String units_label
    ) {
        this.max_altitude = max_altitude;
        this.breakoff_altitude = breakoff_altitude;
        this.deploy_altitude = deploy_altitude;
        this.harddeck_altitude = harddeck_altitude;
        this.major_units = major_units;
        this.units_label = units_label;
        // Precompute angle for drawing
        breakoff_angle = (float) (360 * breakoff_altitude / max_altitude);
        deploy_angle   = (float) (360 * deploy_altitude / max_altitude);
        harddeck_angle = (float) (360 * harddeck_altitude / max_altitude);
        major_angle = (float) (2.0 * Math.PI * major_units / max_altitude);
    }

    public static final AnalogAltimeterOptions imperial = new AnalogAltimeterOptions(
            12000 * Convert.FT,
            4000 * Convert.FT,
            3000 * Convert.FT,
            2000 * Convert.FT,
            1000 * Convert.FT,
            "x1000ft"
    );
    public static final AnalogAltimeterOptions metric = new AnalogAltimeterOptions(
            4000,
            1333,
            1000,
            666,
            1000,
            "x1000m"
    );

}
