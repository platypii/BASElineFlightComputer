package com.platypii.baseline.location;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.platypii.baseline.data.measurements.MLocation;
import java.util.ArrayList;
import java.util.List;

abstract class LocationProvider {
    private static final String TAG = "LocationService";

    // Listeners
    private final List<MyLocationListener> listeners = new ArrayList<>();

    // GPS status
    public float refreshRate = 0; // Moving average of refresh rate in Hz

    // Satellite data
    public int satellitesInView = -1; // Satellites in view
    public int satellitesUsed = -1; // Satellites used in last fix

    // phone time = GPS time + offset
    private long phoneOffsetMillis = 0;

    // Computed parameters
    public double vD = 0;

    // History
    public MLocation lastLoc; // last location received
    // private MLocation prevLoc; // 2nd to last

    /**
     * Start location updates
     * @param context The Application context
     */
    public abstract void start(@NonNull Context context);

    /**
     * Returns the number of milliseconds since the last fix
     */
    public long lastFixDuration() {
        if(lastLoc != null && lastLoc.millis > 0) {
            final long duration = System.currentTimeMillis() - (lastLoc.millis + phoneOffsetMillis);
            if(duration < 0) {
                Log.w(TAG, "Time since last fix should never be negative");
            }
            return duration;
        } else {
            return -1;
        }
    }

    /**
     * Add a new listener to be notified of location updates
     */
    public void addListener(MyLocationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener from location updates
     */
    public void removeListener(MyLocationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Children should call updateLocation() when they have new location information
     */
    void updateLocation(MLocation loc) {

        // Store location
        final MLocation prevLoc = lastLoc;
        lastLoc = loc;

        // Log.v(TAG, "MyLocationManager.updateLocation(" + lastLoc + ")");

        if (prevLoc != null) {
            // Compute vertical speed
            vD = -1000.0 * (lastLoc.altitude_gps - prevLoc.altitude_gps) / (lastLoc.millis - prevLoc.millis);

            // GPS sample refresh rate
            // TODO: Include time from last sample until now
            final long deltaTime = lastLoc.millis - prevLoc.millis; // time since last refresh
            if (deltaTime > 0) {
                final float refreshTime = 1000.0f / (float) (deltaTime);
                refreshRate += (refreshTime - refreshRate) * 0.5f; // Moving average
                if (Double.isNaN(refreshRate)) {
                    Log.e(TAG, "Refresh rate is NaN, deltaTime = " + deltaTime + " refreshTime = " + refreshTime);
                    refreshRate = 0;
                }
            }
        }

        // Update gps time offset
        phoneOffsetMillis = System.currentTimeMillis() - lastLoc.millis;

        // Notify listeners (using AsyncTask so the manager never blocks!)
        new AsyncTask<MLocation, Void, Void>() {
            @Override
            protected Void doInBackground(MLocation... params) {
                synchronized (listeners) {
                    for (MyLocationListener listener : listeners) {
                        listener.onLocationChanged(params[0]);
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                for(MyLocationListener listener : listeners) {
                    listener.onLocationChangedPostExecute();
                }
            }
        }.execute(lastLoc);
    }

    public void stop() {
        if(listeners.size() > 0) {
            Log.e(TAG, "Stopping location service, but listeners are still listening");
        }
    }
}
