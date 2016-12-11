package com.platypii.baseline.alti;


import java.util.Locale;

/**
 * An altitude measurement
 */
public class MAltitude {
    public final String sensor = "Alt";

    // Altimeter
    public long millis; // Milliseconds since epoch
    public long nano; // Nanoseconds since boot
    public final double altitude;  // Altitude (m)
    public final double climb;     // Rate of climb (m/s)
    public final double pressure;  // Barometric pressure (hPa)

    public MAltitude(long millis, long nano, double altitude, double climb, float pressure) {
        this.millis = millis;
        this.nano = nano;
        this.altitude = altitude;
        this.climb = climb;
        this.pressure = pressure;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "MAltitude(%d,%.1f)", millis, altitude);
    }

}
