package com.platypii.baseline.audible;

import com.platypii.baseline.audible.ModeEvent;
import com.platypii.baseline.audible.SensorEvent;
import com.platypii.baseline.ui.Convert;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class EventDatabase {

    private static final String DATABASE_NAME = "Events.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "Events";

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    // Events
    public HashMap<String,Event> events = new HashMap<String,Event>();
    public List<Event> eventList = new ArrayList<Event>();
    
    // Special values (TODO: Read special values from DB)
    public double breakoff_altitude = 4500 * Convert.FT;
    public double deploy_altitude = 3500 * Convert.FT;
    public double harddeck_altitude = 1800 * Convert.FT;
    public double seatbelt_altitude = 1800 * Convert.FT;


    public EventDatabase(Context context) {
        databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
        
        // Cursor cur = managedQuery(People.CONTENT_URI, PROJECTION, null, null);
        // SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this, R.layout.list_item, cur, new String[] {"_id"}, new int[] {R.id.idLabel});
        // startManaging(cursor);
        
        // Load from database
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        while(cursor.moveToNext()) {
            // Load each row from cursor
            Event event = new Event(cursor);
            events.put(event.id, event);
            eventList.add(event);
        }
    }

    /**
     * Retrieve an event by id
     */
    public Event get(String id) {
        return events.get(id);
        
        // Cursor cursor = getReadableDatabase().rawQuery("select * from "+TABLE_NAME+" where _id = ?", new String[] { id });
        // database.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_CATEGORY, KEY_SUMMARY, KEY_DESCRIPTION }, null, null, null, null, null);

    }
    
    /**
     * Add/update an event
     */
    public void put(Event event) {
        if(event == null) throw new IllegalArgumentException();
        
        // Insert event into DB
        /*
        if(events.containsKey(event.id)) {
            // Row exists, update
            db.update(TABLE_NAME, event.getContentValues(), "_id=?", new String[]{event.id});
        } else {
            // New event, insert
            db.insert(TABLE_NAME, null, event.getContentValues());
        }
        */
        try {
        	db.insertOrThrow(TABLE_NAME, null, event.getContentValues());
        } catch(SQLException e) {
        	db.update(TABLE_NAME, event.getContentValues(), "_id=?", new String[]{event.id});
        }

        // Local copy
        if(events.containsKey(event.id)) {
            // Already in list
        } else {
            events.put(event.id, event);
            eventList.add(event);
        }
    }
    
    /**
     * Remove an event
     */
    public void remove(Event event) {
        if(event == null) throw new IllegalArgumentException();
        
        // Remove event from db
        db.delete(TABLE_NAME, "_ID=?", new String [] {event.id});
        
        // Local copy
        events.put(event.id, event);
        eventList.remove(event);
    }
    
    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w("SQLite", "Creating table " + TABLE_NAME);
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
//                       + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                       + "_id TEXT PRIMARY KEY,"
                       + "enabled INTEGER,"
                       + "trigger TEXT,"
                       + "sound TEXT,"
                       + "modifiers TEXT);");

            // Insert default events
            Event defaultEvents[] = new Event[] {
                    new Event("Breakoff", true, new ModeEvent("Freefall"), new SensorEvent("Altitude", deploy_altitude, breakoff_altitude), "Beep 1", false, 0.5f, null, false),
                    new Event("Deployment", true, new ModeEvent("Freefall"), new SensorEvent("Altitude", harddeck_altitude, deploy_altitude), "Beep 1", false, 0.5f, null, false),
                    new Event("Harddeck", true, new ModeEvent("Freefall"), new SensorEvent("Altitude", 0, harddeck_altitude), "Beep 1", true, 0.5f, null, false),

                    new Event("Seatbelt", true, new ModeEvent("Climb"), new SensorEvent("Altitude", seatbelt_altitude, seatbelt_altitude + 200), "Beep 1", false, 0.5f, null, false),

                    new Event("Tracking", false, new ModeEvent("Freefall"), null, "Drum Beat 1", true, 0.5f, new SensorEvent("Glide Angle", -90, -30), true), // vertical to -2:1
                    new Event("Freefall Vario", false, new ModeEvent("Freefall"), null, "Drum Beat 1", true, 0.5f, new SensorEvent("Climb Rate", 0, 134), true), // 0 .. 300mph
                    new Event("Flight Vario", false, new ModeEvent("Flight"), null, "Drum Beat 1", true, 0.5f, new SensorEvent("Glide Angle", -90, 30), true), // vertical to 2:1
            
                    new Event("Driving", false, null, null, "Drum Beat 1", true, 0.5f, new SensorEvent("Speed", 0, 100 * Convert.MPH), true),
                    
                    new Event("GPS Monitor", false, null, null, "Heartbeat", true, 0.5f, null, true),

                    new Event("Homing Left", false, null, new SensorEvent("Bearing Home", -180, 10), "Beep 1", true, 0.0f, null, true),
                    new Event("Homing Right", false, null, new SensorEvent("Bearing Home", 10, 180), "Beep 1", true, 1.0f, null, true),

                    new Event("Startup Sound", true, null, null, "Bass Loop 1", false, 0.5f, null, false),
                    
                    new Event("Test Event 1", false, null, null, "Drum Beat 1", true, 0.5f, new SensorEvent("Tilt", -90, 90), false),
                    new Event("Test Event 2", false, null, null, "Beep 1", true, 0.5f, new SensorEvent("Tilt", 90, -90), false)
            };
            for(Event event : defaultEvents) {
                db.insert(TABLE_NAME, null, event.getContentValues());
            }

        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	Log.w("EventDatabase", "Upgrading Event DB");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}

