package com.platypii.baseline.data;

import com.platypii.baseline.data.measurements.MAltitude;

/**
 * Used by MyAltimeter to notify activities of updated location
 */
public interface MyAltitudeListener {

    /**
     * Process the new reading on a background thread
     */
    void altitudeDoInBackground(MAltitude alt);

    /**
     * Process the new reading on the main thread
     */
    void altitudeOnPostExecute(); // post-execute doesn't get a parameter, because UI threads should just pull the latest data

}
