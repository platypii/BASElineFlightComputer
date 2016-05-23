package com.platypii.baseline.data.measurements;


import java.util.Locale;

/**
 * An altitude measurement
 * @author platypii
 */
public class MAltitude extends Measurement {
    public final String sensor = "Alt";

    // Altimeter
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
    public String toRow() {
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return String.format(Locale.US, "%d,%d,alt,%f", millis, nano, pressure);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "MAltitude(%d,%.1f)", millis, altitude);
    }

}
