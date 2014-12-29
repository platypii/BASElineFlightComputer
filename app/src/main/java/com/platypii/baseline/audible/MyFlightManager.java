package com.platypii.baseline.audible;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.ui.Convert;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("unused") // Dead code blocks for debug mode


/**
 * A class to log, analyze, and provide feedback on flight data
 * @author platypii
 */
public class MyFlightManager {

    // Singleton FlightManager
    private static MyFlightManager _instance;
    private static Context context;

    public static String flightMode = null;
    public static int jumpNumber = -1;
    public static boolean jumping = false; // Has a jump begun?
    
    public static Location homeLoc = null;
    
    private static final boolean DEBUG = false; // TODO: Disable debug mode!
    
    
    // Periodic UI updates
    private static final Handler handler = new Handler();
    private static final int updateInterval = 100; // in milliseconds

    
    /**
     * Initialize flight services
     * @param context The application context
     */
    public static synchronized void initFlight(Context context) {
        if(_instance == null) {
            _instance = new MyFlightManager();
            MyFlightManager.context = context;

            if(DEBUG)
            	Log.w("FlightManager", "DEBUG MODE ENABLED");
            
            // Initialize flight mode
            flightMode = null;
            jumpNumber = MyDatabase.jumps.lastJumpNumber + 1;
            jumping = false;
            
            // Start altitude updates
            MyAltimeter.addListener(new MyAltitudeListener() {    
                public void altitudeDoInBackground(MyAltitude alt) {
                    update();
                }
                public void altitudeOnPostExecute() {}
            });
            
            // Note: Right now the flight mode does not depend on GPS. 
            // This is for reliability reasons and is strongly encouraged.
            // Add GPS listener here if flight mode depends on GPS (speed, glide, etc).
            
            // Periodic UI updates
            handler.post(new Runnable() {
                public void run() {
                    update();
                    handler.postDelayed(this, updateInterval);
                }
            });
        }         
    }
    
    /**
     * Updates the flight manager
     */
    private static void update() {
    	
    	// Set home location on first good fix
    	if(homeLoc == null && MyLocationManager.lastLoc != null) {
    		// TODO: Require minimum accuracy
    		MyFlightManager.homeLoc = MyLocationManager.lastLoc.loc(); // Use last GPS
            // Popup text to notify that home location is set
    		// Use async task to ensure it is called from the UI thread 
            new AsyncTask<Void,Void,Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {
        			Toast.makeText(context, "Home location set", Toast.LENGTH_SHORT).show();
                }
            }.execute();
    	}
    	
        // Check if we need to change modes
        transitionMode();
        
        // Initialize jumps on freefall or flight, and terminate on the ground
        if("Freefall".equals(flightMode) && !jumping) {
            MyDatabase.jumps.startJump(System.currentTimeMillis());
            jumping = true;
        } else if("Ground".equals(flightMode) && jumping) {
            MyDatabase.jumps.endJump(System.currentTimeMillis());
            jumping = false;
            jumpNumber++;
        }
    }
    
    /**
     * State machine for flight modes
     */
	private static void transitionMode() {
        if(flightMode == null || flightMode.equals("")) {
            if(!Double.isNaN(MyAltimeter.altitude))
                flightMode = "Ground";

            // DEBUG MODE: Enter freefall mode when higher than 100m
            } else if(DEBUG && flightMode.equals("Ground")) {
                if(100 < MyAltimeter.altitude) // higher than 100m
                    flightMode = "Freefall";
            // DEBUG MODE: Enter ground mode when lower than 100m
            } else if(DEBUG && flightMode.equals("Freefall")) {
                if(MyAltimeter.altitude < 100) // lower than 100m
                    flightMode = "Ground";

        // Ground mode
        } else if(flightMode.equals("Ground")) {
        	// TODO: If gain more than 100ft in X seconds
            if(3 * Convert.MPH < MyAltimeter.climb) // climbing faster than 3mph
                flightMode = "Climb";
            else if(MyAltimeter.climb < -30 * Convert.MPH) // falling faster than 30mph
                flightMode = "Freefall";

        // Climb mode
        } else if(flightMode.equals("Climb")) {
            if(MyAltimeter.climb < -60 * Convert.MPH)
                // if falling faster than 60mph, change to freefall mode
                flightMode = "Freefall";
            else if(-60 * Convert.MPH <= MyAltimeter.climb && MyAltimeter.climb < -20 * Convert.MPH)
                // if falling slower than 60mph and faster than 20mph, change to flight mode
                flightMode = "Flight";

        // Freefall mode 
        } else if(flightMode.equals("Freefall")) {
            if(Math.abs(MyAltimeter.climb) < 2 * Convert.MPH)
                flightMode = "Ground";
            else if(-60 * Convert.MPH < MyAltimeter.climb && MyAltimeter.climb < -20 * Convert.MPH)
                flightMode = "Flight";
            
        } else if(flightMode.equals("Flight")) {
            if(MyAltimeter.climb < -60 * Convert.MPH)
                flightMode = "Freefall";
            else if(Math.abs(MyAltimeter.climb) < 2 * Convert.MPH)
                flightMode = "Ground";
        }
    }

    /**
     * Computes the estimated time to ground based on current location + velocity
     */
    public static double timeToGround() {
        double timeToGround = -MyAltimeter.altitude / MyAltimeter.climb;
        if(Double.isNaN(timeToGround) || Double.isInfinite(timeToGround) || timeToGround < 0.01 || Math.abs(MyAltimeter.climb) < 0.01 * Convert.MPH || 24 * 60 * 60 < timeToGround) {
            // return NaN if we don't have an accurate landing location (climbing, very close to ground, very long estimate, etc)
            return Double.NaN;
        } else {
            return timeToGround;
        }
    }

    /**
     * Computes the estimated landing location based on current location + velocity
     */
    public static Location getLandingLocation() {
        // Compute time to ground
        double timeToGround = timeToGround();
        if(Double.isNaN(timeToGround)) {
            return null;
        } else {
            // Compute horizontal distance traveled at current velocity for timeToGround seconds
            final double groundDistance = timeToGround * MyLocationManager.groundSpeed;
            final double bearing = MyLocationManager.bearing;

            // Compute estimated landing location
            com.platypii.baseline.data.MyLocation currentLocation = MyLocationManager.lastLoc;
            final Location landingLocation = currentLocation.moveDirection(bearing, groundDistance);

//            Log.d("FlightManager", currentLocation + " -> " + landingLocation + " (" + groundDistance + "m, " + bearing + "°)");

            return landingLocation;
        }
    }

}



