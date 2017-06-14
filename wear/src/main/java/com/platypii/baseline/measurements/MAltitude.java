package com.platypii.baseline.measurements;


import java.util.Locale;

/**
 * An altitude measurement
 */
public class MAltitude {
    public final String sensor = "Alt";

    // Altimeter
    public long millis; // Milliseconds since epoch
    public final double altitude;  // Altitude (m)
    public final double climb;     // Rate of climb (m/s)

    public MAltitude(long millis, double altitude, double climb) {
        this.millis = millis;
        this.altitude = altitude;
        this.climb = climb;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "MAltitude(%d,%.1f)", millis, altitude);
    }

}
