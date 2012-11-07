package com.platypii.baseline.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class SensorDatabase {

    private static final String DATABASE_NAME = "Sensors.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "Sensors";

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;


    public SensorDatabase(Context context) {
        databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
        
        // Start altimeter updates
        MyAltimeter.addListener(new MyAltitudeListener() {
            public void doInBackground(MyAltitude measure) {
                // Log to database
                saveMeasurement(measure);
            }
            public void onPostExecute() {}
        });
        // Start location updates
        MyLocationManager.addListener(new MyLocationListener() {
            public void onLocationChanged(MyLocation measure) {
                // Log to database
                saveMeasurement(measure);
            }
        });
        // TODO: Start sensor updates?
        
    }

    /**
     * Logs a measurement to the database
     * @param measure the measurement to store
     */
    public void saveMeasurement(Measurement measure) {
        if(measure != null) {
        	db.insert(TABLE_NAME, null, measure.getContentValues());
        }
    }
    
    /**
     * Returns all the measurements between timeStart and timeEnd
     * Warning: EXPENSIVE!
     * 
     * @param timeStart The beginning time of the data to return
     * @param timeEnd The end time of the data to return
     * @return All data for all rows within the time period
     */
    public Cursor queryAll(Jump jump) {
        Cursor cursor = null;
    	if(jump.jumpNumber < 0) {
    		cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
    	} else {
    		cursor = db.query(TABLE_NAME, null, "? <= millis AND millis < ?", new String[]{Long.toString(jump.timeStart),Long.toString(jump.timeEnd)}, null, null, null);
    	}
    	Log.w("Sensor Query", jump.jumpName + ", rows = " + cursor.getCount());
    	return cursor;
    }
    
    /**
     * Returns location data between timeStart and timeEnd
     * Warning: EXPENSIVE!
     */
    public Cursor queryTrack(Jump jump) {
    	Cursor cursor = db.query(TABLE_NAME, new String[]{"millis","altitude","latitude","longitude","flightMode"}, 
    							 "? <= millis AND millis < ? AND sensor = ?", new String[]{Long.toString(jump.timeStart),Long.toString(jump.timeEnd),"GPS"},
    							 null, null, null);
    	Log.w("Sensor Query", jump.jumpName + ", rows = " + cursor.getCount());
    	return cursor;
    }

    /**
     * Returns altitude data between timeStart and timeEnd
     * Warning: EXPENSIVE!
     */
    public Cursor queryJump(Jump jump) {
    	Cursor cursor = db.query(TABLE_NAME, new String[]{"millis","altitude","flightMode"}, 
    							 "? <= millis AND millis < ? AND sensor = ?", new String[]{Long.toString(jump.timeStart),Long.toString(jump.timeEnd),"GPS"},
    							 null, null, null);
    	Log.w("Sensor Query", jump.jumpName + ", rows = " + cursor.getCount());
    	return cursor;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w("SQLite", "Creating table " + TABLE_NAME);
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                       + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"

                       + "millis INTEGER,"
                       + "latitude REAL,"
                       + "longitude REAL,"
                       + "altitude REAL,"

                       + "sensor TEXT,"
                       // Altimeter
                       //+ "nano INTEGER,"
                       + "climb REAL,"
                       + "pressure REAL,"
                       // GPS
                       + "altitude_gps REAL,"
                       + "vN REAL,"
                       + "vE REAL,"
                       + "hAcc REAL,"
                       //+ "vAcc REAL,"
                       //+ "sAcc REAL,"
                       + "pdop REAL,"
                       + "hdop REAL,"
                       + "vdop REAL,"
                       + "numSat INTEGER,"
                       + "flightMode TEXT,"
                       + "gX REAL,"
                       + "gY REAL,"
                       + "gZ REAL,"
                       + "rotX REAL,"
                       + "rotY REAL,"
                       + "rotZ REAL,"
                       + "acc REAL);");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	// TODO: Migrate data!
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}

