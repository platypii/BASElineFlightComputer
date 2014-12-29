package com.platypii.baseline.data;

import com.platypii.baseline.audible.MyFlightManager;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * A generic measurement (alti, gps, gyro, etc)
 * @author platypii
 */
public class Measurement {
    
    public long timeMillis; // Milliseconds since epoch
    
    public String sensor;
    
    // Altimeter
    //public long timeNano = -1; // Nanoseconds reported by android SensorEvent
    public double altitude = Double.NaN; // Altitude
    public double climb = Double.NaN; // Rate of climb
    public double pressure = Double.NaN; // Barometric pressure (hPa)
    
    // GPS
    public double latitude = Double.NaN; // Latitude
    public double longitude = Double.NaN; // Longitude
    public double altitude_gps = Double.NaN; // GPS altitude MSL
    public double vN = Double.NaN; // Velocity north
    public double vE = Double.NaN; // Velocity east
    public float hAcc = Float.NaN; // Horizontal accuracy
    //public float vAcc = Float.NaN; // Vertical accuracy
    //public float sAcc = Float.NaN; // Speed accuracy
    public float pdop = Float.NaN; // Positional dilution of precision
    public float hdop = Float.NaN; // Horizontal dilution of precision
    public float vdop = Float.NaN; // Vertical dilution of precision
    public int numSat = -1; // Number of satellites
    public float groundDistance = Float.NaN; // Ground distance since app started

    // State data
    public String flightMode = null; // Flight mode

    // Sensors
    public float gX = Float.NaN;
    public float gY = Float.NaN;
    public float gZ = Float.NaN;
    public float rotX = Float.NaN;
    public float rotY = Float.NaN;
    public float rotZ = Float.NaN;
    public float acc = Float.NaN;
    

    public Measurement() {}
    
    public Measurement(Cursor c) {
        String cols[] = c.getColumnNames();
        for(int i = 0; i < cols.length; i++) {
            if(cols[i].equals("millis")) {
                timeMillis = c.getLong(i);
            } else if(cols[i].equals("sensor")) {
                sensor = c.getString(i);
            } else if(cols[i].equals("altitude")) {
            	altitude = c.getDouble(i);
            } else if(cols[i].equals("climb")) {
            	climb = c.getDouble(i);
            } else if(cols[i].equals("pressure")) {
            	pressure = c.getDouble(i);

            } else if(cols[i].equals("latitude")) {
            	latitude = c.getDouble(i);
            } else if(cols[i].equals("longitude")) {
            	longitude = c.getDouble(i);
            } else if(cols[i].equals("altitude_gps")) {
            	altitude_gps = c.getDouble(i);
            } else if(cols[i].equals("vN")) {
            	vN = c.getDouble(i);
            } else if(cols[i].equals("vE")) {
            	vE = c.getDouble(i);
            } else if(cols[i].equals("hAcc")) {
            	hAcc = c.getFloat(i);
            } else if(cols[i].equals("pdop")) {
            	pdop = c.getFloat(i);
            } else if(cols[i].equals("hdop")) {
            	hdop = c.getFloat(i);
            } else if(cols[i].equals("vdop")) {
            	vdop = c.getFloat(i);
            } else if(cols[i].equals("numSat")) {
            	numSat = c.getInt(i);

            } else if(cols[i].equals("flightMode")) {
            	flightMode = c.getString(i);
            } else if(cols[i].equals("gX")) {
            	gX = c.getFloat(i);
            } else if(cols[i].equals("gY")) {
            	gY = c.getFloat(i);
            } else if(cols[i].equals("gZ")) {
            	gZ = c.getFloat(i);
            } else if(cols[i].equals("rotX")) {
            	rotX = c.getFloat(i);
            } else if(cols[i].equals("rotY")) {
            	rotY = c.getFloat(i);
            } else if(cols[i].equals("rotZ")) {
            	rotZ = c.getFloat(i);
            } else if(cols[i].equals("acc")) {
            	acc = c.getFloat(i);
            }

        }
    }

    /**
     * Loads the global state from various managers (flightMode, orientation, etc)
     */
    public void loadState() {
        this.flightMode = MyFlightManager.flightMode;
        if(MySensorManager.gravity != null) {
	        MySensorEvent grav = MySensorManager.gravity.lastSensorEvent;
	        if(grav != null) {
		        this.gX = grav.x;
		        this.gY = grav.y;
		        this.gZ = grav.z;
	        }
        }
        if(MySensorManager.rotation != null) {
        	MySensorEvent rot = MySensorManager.rotation.lastSensorEvent;
	        if(rot != null) {
		        this.rotX = rot.x;
		        this.rotY = rot.y;
		        this.rotZ = rot.z;
	        }
        }
        if(MySensorManager.accel != null) {
	        MySensorEvent accel = MySensorManager.accel.lastSensorEvent;
	        if(accel != null) {
	        	this.acc = (float)Math.sqrt(accel.x * accel.x + accel.y * accel.y + accel.z * accel.z);
	        }
        }
    }
    
    // Computed parameters
    // Ground velocity
    public double groundSpeed() {
    	return Math.sqrt(vN * vN + vE * vE);
    }
    // Total 3D velocity
    public double speed() {
    	return Math.sqrt(vN * vN + vE * vE + climb * climb);
    }
    // Glide ratio
    public double glideRatio() {
    	return Math.sqrt(vN * vN + vE * vE) / climb;
    }
    //public double bearing() {return Math.atan2(vN, vE);} // TODO: Bearing in degrees
    //public double glideAngle = Double.NaN; // TODO: Glide angle in degrees (freefall = -90, level flight = 0)

    /**
     * Returns a ContentValues (database row) representing this measurement
     * @return A ContentValues object representing this measurement as a row
     */
    public ContentValues getContentValues() {
        ContentValues values  = new ContentValues();
        // values.put("_id", timeMillis);

        // t,x,y,z
        values.put("millis", timeMillis);
        if(!Double.isNaN(latitude)) values.put("latitude", latitude);
        if(!Double.isNaN(longitude)) values.put("longitude", longitude);
        if(!Double.isNaN(altitude)) values.put("altitude", altitude);
        
        values.put("sensor", sensor);
        // Altimeter
        if(!Double.isNaN(climb)) values.put("climb", climb); // TODO: Use velocity down instead of climb to stay compatible with FlySight?
        if(!Double.isNaN(pressure)) values.put("pressure", pressure);
        // GPS
        if(!Double.isNaN(altitude_gps)) values.put("altitude_gps", altitude_gps);
        if(!Double.isNaN(vN)) values.put("vN", vN);
        if(!Double.isNaN(vE)) values.put("vE", vE);
        if(!Double.isNaN(hAcc)) values.put("hAcc", hAcc);
        if(!Double.isNaN(hdop)) values.put("hdop", hdop);
        if(!Double.isNaN(vdop)) values.put("vdop", vdop);
        if(!Double.isNaN(pdop)) values.put("pdop", pdop);
        if(numSat != -1) values.put("numSat", numSat);
        // Global state
        if(flightMode != null) values.put("flightMode", flightMode);
        if(!Float.isNaN(gX)) values.put("gX", gX);
        if(!Float.isNaN(gY)) values.put("gY", gY);
        if(!Float.isNaN(gZ)) values.put("gZ", gZ);
        if(!Float.isNaN(rotX)) values.put("rotX", rotX);
        if(!Float.isNaN(rotY)) values.put("rotY", rotY);
        if(!Float.isNaN(rotZ)) values.put("rotZ", rotZ);
        if(!Float.isNaN(acc)) values.put("acc", acc);
        return values;
    }
    
}

