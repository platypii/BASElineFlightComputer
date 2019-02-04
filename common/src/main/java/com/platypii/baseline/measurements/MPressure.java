package com.platypii.baseline.measurements;

import com.platypii.baseline.util.Numbers;
import android.support.annotation.NonNull;
import java.util.Locale;

/**
 * An barometric pressure measurement
 */
public class MPressure extends Measurement {
    public final String sensor = "Baro";

    // Altimeter
    public final double altitude;  // Pressure altitude (m)
    public final double climb;     // Rate of climb (m/s)
    public final double pressure;  // Barometric pressure (hPa)

    public MPressure(long millis, long nano, double altitude, double climb, float pressure) {
        this.millis = millis;
        this.nano = nano;
        this.altitude = altitude;
        this.climb = climb;
        this.pressure = pressure;
    }

    @NonNull
    @Override
    public String toRow() {
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return millis + "," + nano + ",alt," + Numbers.format3.format(pressure);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "MPressure(%d,%.2f)", millis, pressure);
    }

}
