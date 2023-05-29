package com.platypii.baseline.location;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.util.PubSub;
import com.platypii.baseline.util.RefreshRateEstimator;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

public abstract class LocationProvider {
    // Duration until location considered stale, in milliseconds
    private static final long LOCATION_TTL = 10000;

    // Moving average of refresh rate in Hz
    public final RefreshRateEstimator refreshRate = new RefreshRateEstimator();

    // History
    public MLocation lastLoc; // last location received
    private MLocation prevLoc; // 2nd to last

    public final PubSub<MLocation> locationUpdates = new PubSub<>();

    /**
     * Give a useful name to the inherited provider
     */
    @NonNull
    protected abstract String providerName();

    /**
     * String description of the GPS device
     */
    @NonNull
    protected abstract String dataSource();

    /**
     * Start location updates
     *
     * @param context The Application context
     */
    public abstract void start(@NonNull Context context);

    /**
     * Returns the number of milliseconds since the last fix
     */
    public long lastFixDuration() {
        if (lastLoc != null && lastLoc.millis > 0) {
            final long duration = System.currentTimeMillis() - TimeOffset.gpsToPhoneTime(lastLoc.millis);
            if (duration < 0) {
                Log.w(providerName(), "Time since last fix should never be negative delta = " + duration + "ms");
            }
            return duration;
        } else {
            return -1;
        }
    }

    /**
     * Returns whether the last location fix is recent
     */
    public boolean isFresh() {
        return lastLoc != null && lastFixDuration() < LOCATION_TTL;
    }

    /**
     * Children should call updateLocation() when they have new location information
     */
    void updateLocation(@NonNull MLocation loc) {
//        Log.v(providerName(), "LocationProvider.updateLocation(" + loc + ")");

        // Check for duplicate
        if (lastLoc != null && lastLoc.equals(loc)) {
            Log.w(providerName(), "Skipping duplicate location " + loc);
            return;
        }
        // Check for negative time delta between locations
        if (lastLoc != null && loc.millis < lastLoc.millis) {
            Log.e(providerName(), "Non-monotonic time delta: " + loc.millis + " - " + lastLoc.millis + " = " + (loc.millis - lastLoc.millis) + " ms");
        }

        // Store location
        prevLoc = lastLoc;
        lastLoc = loc;

        // Update gps time offset
        // TODO: What if there are multiple GPS devices giving different times?
        TimeOffset.update(providerName(), lastLoc.millis);

        refreshRate.addSample(lastLoc.millis);

        // Notify listeners (async so the service never blocks!)
        locationUpdates.postAsync(lastLoc);
    }

    /**
     * Helper method for getting latest speed in m/s
     * If GPS is not giving us speed natively, fallback to computing from v = dist/time.
     * This should be used for display of the latest groundspeed, but not for logging.
     */
    public double groundSpeed() {
        if (isFresh()) {
            final double lastGroundSpeed = lastLoc.groundSpeed();
            if (Numbers.isReal(lastGroundSpeed)) {
                return lastGroundSpeed;
            } else {
                // Compute ground speed from previous location
                if (prevLoc != null) {
                    final double dist = prevLoc.distanceTo(lastLoc);
                    final double dt = (lastLoc.millis - prevLoc.millis) * 0.001;
                    if (dt > 0) {
                        return dist / dt;
                    }
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Helper method for getting latest speed in m/s
     * If GPS is not giving us speed natively, fallback to computing from v = dist/time.
     * This should be used for display of the latest total speed, but not for logging.
     */
    public double totalSpeed() {
        if (isFresh()) {
            return lastLoc.totalSpeed();
        } else {
            return Double.NaN;
        }
    }

    /**
     * Helper method for getting latest bearing in degrees
     * If GPS is not giving us speed natively, fallback to computing from v = dist/time.
     * This should be used for display of the latest groundspeed, but not for logging.
     */
    public double bearing() {
        if (isFresh()) {
            final double lastBearing = lastLoc.bearing();
            if (Numbers.isReal(lastBearing)) {
                return lastBearing;
            } else {
                // Compute ground speed from previous location
                if (prevLoc != null) {
                    return prevLoc.bearingTo(lastLoc);
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Helper method for getting latest speed in m/s
     * If GPS is not giving us speed natively, fallback to computing from v = dist/time.
     * This should be used for display of the latest total speed, but not for logging.
     */
    public double glideRatio() {
        if (isFresh()) {
            return lastLoc.glideRatio();
        }
        return Double.NaN;
    }

    /**
     * Reset the state of the location provider.
     * This is useful when switching devices.
     */
    public void reset() {
        lastLoc = null;
        prevLoc = null;
        refreshRate.reset();
    }

    public void stop() {
        if (!locationUpdates.isEmpty()) {
            Log.w(providerName(), "Stopping location service, but listeners are still listening");
        }
    }
}
