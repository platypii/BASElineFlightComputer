package com.platypii.baseline.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.Services;
import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.data.measurements.Measurement;
import com.platypii.baseline.location.MyLocationListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

/**
 * Logs altitude and location data to the database. Also contains event and jump databases
 * AKA- The Black Box flight recorder
 */
public class MyDatabase implements MyLocationListener, MySensorListener {

    // Singleton database when logging
    private static MyDatabase db = null;

    private final long startTime = System.nanoTime();
    private long stopTime = -1;

    // Log file
    private final File logFile;
    private final BufferedWriter log;
    
    public static synchronized void startLogging(@NonNull Context appContext) {
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
    public static synchronized Jump stopLogging() {
        if(db != null) {
            // Stop logging
            final File logFile = db.stop();
            db = null;
            if(logFile != null) {
                return new Jump(logFile);
            } else {
                return null;
            }
        } else {
            Log.e("DB", "stopLogging() called when database isn't logging");
            return null;
        }
    }

    public static boolean isLogging() {
        return db != null;
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
            return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis);
        } else {
            return "0:00.000";
        }
    }


    private MyDatabase(@NonNull Context appContext) throws IOException {
        // Open log file for writing
        final File logDir = JumpLog.getLogDirectory(appContext);
        final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        final String timestamp = dt.format(new Date());

        // gzip log file
        logFile = new File(logDir, "track_" + timestamp + ".csv.gz");
        log = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(logFile))));

        // Write header
        log.write(Measurement.header + "\n");

        // Start sensor updates
        EventBus.getDefault().register(this);
        Services.location.addListener(this);
        MySensorManager.addListener(this);

        Log.i("DB", "Logging to " + logFile);
    }

    private File stop() {
        if(stopTime == -1) {
            stopTime = System.nanoTime();

            // Stop sensor updates
            EventBus.getDefault().unregister(this);
            Services.location.removeListener(this);
            MySensorManager.removeListener(this);

            // Close file writer
            try {
                log.close();
                Log.i("DB", "Logging stopped for " + logFile.getName());
                return logFile;
            } catch (IOException e) {
                Log.e("DB", "Failed to close log file " + logFile, e);
                FirebaseCrash.report(e);
                return null;
            }
        } else {
            Log.e("DB", "Logging stopped twice");
            return null;
        }
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAltitudeEvent(MAltitude alt) {
        if(!Double.isNaN(alt.pressure)) {
            logLine(alt.toRow());
        }
    }

    // Location listener
    @Override
    public void onLocationChanged(@NonNull MLocation measure) {
        if(!Double.isNaN(measure.latitude) && !Double.isNaN(measure.longitude)) {
            logLine(measure.toRow());
        }
    }
    @Override
    public void onLocationChangedPostExecute() {}

    // Sensor listener
    public void onSensorChanged(@NonNull Measurement measure) {
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
                FirebaseCrash.report(e);
            }
        } else {
            Log.e("DB", "Attempted to log after closing file");
        }
    }

}
