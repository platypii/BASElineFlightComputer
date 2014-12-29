package com.platypii.baseline.audible;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;

import android.os.Handler;


// A class to analyze flight data and provide feedback
public class MyAudible {

    // Singleton FlightManager
    private static MyAudible _instance;

    
    // Periodic UI updates
    private static final Handler handler = new Handler();
    private static final int updateInterval = 100; // in milliseconds

    
    /**
     * Initialize flight services
     */
    public static synchronized void initAudible() {
        if(_instance == null) {
            _instance = new MyAudible();
        
            // Start altitude updates
            MyAltimeter.addListener(new MyAltitudeListener() {    
                public void altitudeDoInBackground(MyAltitude alt) {
                    update();
                }
                public void altitudeOnPostExecute() {}
            });
            
            // GPS updates
            MyLocationManager.addListener(new MyLocationListener() {
                public void onLocationChanged(final MyLocation loc) {
                    update();
                }
            });
            
            // Periodically check event triggers
            handler.post(new Runnable() {
                public void run() {
                    update();
                    handler.postDelayed(this, updateInterval);
                }
            });
        }         
    }
    
    private static void update() {
        // Check event triggers
        for(Event event : MyDatabase.events.events.values()) {
            event.update();
        }
    }

}



