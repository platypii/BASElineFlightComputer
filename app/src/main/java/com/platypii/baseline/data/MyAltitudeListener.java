package com.platypii.baseline.data;


/**
 * Used by MyAltimeter to notify activities of updated location
 * @author platypii
 */
public interface MyAltitudeListener {
    
	/**
	 * Process the new reading on a background thread
	 */
    public void altitudeDoInBackground(MyAltitude alt);

	/**
	 * Process the new reading on the UI thread
	 */
    public void altitudeOnPostExecute(); // post-execute doesn't get a parameter, because UI threads should just pull the latest data

}
