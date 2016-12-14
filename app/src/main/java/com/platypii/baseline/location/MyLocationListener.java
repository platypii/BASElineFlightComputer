package com.platypii.baseline.location;

import com.platypii.baseline.measurements.MLocation;

/**
 * Used by MyLocationManager to notify activities of updated location
 */
public interface MyLocationListener {

    /**
     * Process the new location on a background thread
     */
    void onLocationChanged(MLocation loc);

    /**
     * Process the new location on the UI thread
     * PostExecute doesn't get a parameter, because UI threads should just pull the latest data
     */
    void onLocationChangedPostExecute();

}
