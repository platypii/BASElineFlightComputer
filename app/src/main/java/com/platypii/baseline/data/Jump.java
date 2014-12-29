package com.platypii.baseline.data;

import java.util.ArrayList;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;


public class Jump implements Parcelable {

	// Jump summary
    public final String jumpName;
    public final int jumpNumber;
    
    public final long timeStart; // The start time of the jump (beginning of climb mode)
    public long timeEnd; // The end time of the jump (return to ground mode)

    // Jump data (only loaded on-demand)
    public boolean loaded = false;
	public final ArrayList<Measurement> jumpData = new ArrayList<>();
    public double exitAlt = Double.NaN;
    public long dataStart = Long.MAX_VALUE; // First data point
    public long dataEnd = Long.MIN_VALUE; // Last data point
    private long freefallStart; // Beginning of freefall
    private long freefallEnd; // End of freefall
    public long freefallTime = 0;
    
    

    public Jump(String jumpName, int jumpNumber, long timeStart, long timeEnd) {
        this.jumpName = jumpName;
        this.jumpNumber = jumpNumber;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }
    
    public Jump(Cursor c) {
        jumpNumber = c.getInt(0);
        jumpName = c.getString(1);
        timeStart = c.getLong(2);
        timeEnd = c.getLong(3);
    }
    
    /**
     * Load or reload jump from Sensor Database
     */
    public void loadJump() {
    	loaded = false;
    	synchronized(jumpData) {
	    	// Initialize
    		jumpData.clear();
	    	exitAlt = Double.NaN;
			freefallTime = 0;
	        // Load jump data from database
	        Cursor cursor = MyDatabase.sensors.queryJump(this);
	        boolean inFreefall = false;
	        Measurement measurement = null;
	        while(cursor.moveToNext()) {
	        	// Read row from database
	        	measurement = new Measurement(cursor); 
	        	// Add to jump data
	        	jumpData.add(measurement);
	        	// Compute stats
	        	if(!inFreefall && "Freefall".equals(measurement.flightMode)) {
	        		// Begin freefall 
	        		inFreefall = true;
	        		freefallStart = measurement.timeMillis;
	        	} else if(inFreefall && !"Freefall".equals(measurement.flightMode)) {
	        		// End freefall
	        		inFreefall = false;
	        		freefallEnd = measurement.timeMillis;
	        		freefallTime += (freefallEnd - freefallStart);
	        	}
	        	// Compute stats
	        	if(Double.isNaN(exitAlt) || exitAlt < measurement.altitude)
	        		exitAlt = measurement.altitude;
	        	if(measurement.timeMillis < dataStart) dataStart = measurement.timeMillis;
	        	if(measurement.timeMillis > dataEnd) dataEnd = measurement.timeMillis;
	        }
	    	if(inFreefall) {
	    		// If we finish the jump in freefall mode
	    		freefallEnd = measurement.timeMillis;
	    		freefallTime += (freefallEnd - freefallStart);
	    	}
	    	loaded = true;
    	}
    }

    // Parcelable
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(jumpNumber);
        p.writeString(jumpName);
        p.writeLong(timeStart);
        p.writeLong(timeEnd);
    }

    // Returns a ContentValues (database row) representing this event
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put("_id", jumpNumber);
        values.put("jump_name", jumpName);
        values.put("time_start", timeStart);
        values.put("time_end", timeEnd);
        return values;
    }

    @Override
    public String toString() {
        return jumpName;
    }
}
