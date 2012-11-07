package com.platypii.baseline.data;

import android.content.Context;

import com.platypii.baseline.audible.EventDatabase;


// Logs altitude and location data to the database. Also contains event and jump databases
// AKA- The Black Box flight recorder
public class MyDatabase {
    
    // Singleton Database
    private static MyDatabase _instance;
    
    // Databases
    public static SensorDatabase sensors;
    public static JumpDatabase jumps;
    public static EventDatabase events;


    /**
     * Initializes database services, if not already running
     * 
     * @param context The Application context
     */
    public static synchronized void initDatabase(Context context) {
        if(_instance == null) {
            _instance = new MyDatabase();
            
            // Open databases
            sensors = new SensorDatabase(context);
            jumps = new JumpDatabase(context);
            events = new EventDatabase(context);
            
        }
    }

}
