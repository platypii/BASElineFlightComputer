package com.platypii.baseline.data;

import android.content.Context;
import android.util.Log;

import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.data.measurements.Measurement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


// Logs altitude and location data to the database. Also contains event and jump databases
// AKA- The Black Box flight recorder
public class MyDatabase implements MyAltitudeListener, MyLocationListener, MySensorListener {

    // Singleton database when logging
    private static MyDatabase db = null;

    private final long startTime = System.nanoTime();
    private long stopTime = -1;

    // Log file
    private final File logFile;
    private final BufferedWriter log;
    
    public static synchronized void startLogging(Context appContext) {
        if(db == null) {
            try {
                db = new MyDatabase(appContext);
            } catch(IOException e) {
                Log.e("DB", "Error starting logging", e);
            }
        } else {
            Log.e("DB", "startLogging() called when database already logging");
        }
    }
    public static synchronized void stopLogging() {
        if(db != null) {
            // Stop logging
            db.stop();
            db = null;
        } else {
            Log.e("DB", "stopLogging() called when database isn't logging");
        }
    }

    MyDatabase(Context appContext) throws IOException {
        // Open log file for writing
        final File logDir = JumpLog.getLogDirectory(appContext);
        final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        final String timestamp = dt.format(new Date());

        // TODO: gzip log file
        // logFile = new File(logDir, "jump-" + timestamp + ".csv.gz");
        // log = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(logFile))));
        logFile = new File(logDir, "jump_" + timestamp + ".csv");
        log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)));

        // Write header
        log.write("timeMillis,sensor,altitude,climb,pressure,latitude,longitude,altitude_gps,gX,gY,gZ,rotX,rotY,rotZ,acc\n");

        // Start sensor updates
        MyAltimeter.addListener(this);
        MyLocationManager.addListener(this);
        MySensorManager.addListener(this);

        Log.i("DB", "Logging to " + logFile);
    }

    public static String getLogTime() {
        if(db != null) {
            long nanoTime;
            if (db.stopTime == -1) {
                nanoTime = System.nanoTime() - db.startTime;
            } else {
                nanoTime = db.stopTime - db.startTime;
            }
            final long millis = (nanoTime / 1000000L) % 1000;
            final long seconds = (nanoTime / 1000000000L) % 60;
            final long minutes = nanoTime / 60000000000L;
            return String.format("%d:%02d.%03d", minutes, seconds, millis);
        } else {
            return "0:00.000";
        }
    }

    void stop() {
        if(stopTime == -1) {
            stopTime = System.nanoTime();

            // Stop sensor updates
            MyAltimeter.removeListener(this);
            MyLocationManager.removeListener(this);
            MySensorManager.removeListener(this);

            // Close file writer
            try {
                log.close();
            } catch (IOException e) {
                Log.e("DB", "Failed to close log file " + logFile, e);
            }
            Log.i("DB", "Logging stopped for " + logFile.getName());
        } else {
            Log.e("DB", "Logging stopped twice");
        }
    }

    // Altitude listener
    public void altitudeDoInBackground(MAltitude measure) {
        logLine(measure.toRow());
    }
    public void altitudeOnPostExecute() {}

    // Location listener
    public void onLocationChanged(MLocation measure) {
        if(!Double.isNaN(measure.latitude) && !Double.isNaN(measure.longitude)) {
            logLine(measure.toRow());
        }
    }

    // Sensor listener
    public void onSensorChanged(Measurement measure) {
        logLine(measure.toRow());
    }

    /**
     * Logs a measurement to the database
     * @param line the measurement to store
     */
    private synchronized void logLine(String line) {
        if(stopTime == -1) {
            try {
                log.write(line);
                log.write('\n');
            } catch (IOException e) {
                Log.e("DB", "Failed to write to log file " + logFile, e);
            }
        } else {
            Log.e("DB", "Attempted to log after closing file");
        }
    }

}
