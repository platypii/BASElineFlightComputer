package com.platypii.baseline.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.measurements.Measurement;
import com.platypii.baseline.sensors.MySensorListener;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
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
public class TrackLogger implements MyLocationListener, MySensorListener {
    private static final String TAG = "TrackLogger";

    private boolean logging = false;

    private long startTimeMillis = System.currentTimeMillis();
    private long startTimeNano = System.nanoTime();
    private long stopTimeNano = -1;

    // Log file
    private File logFile;
    private BufferedWriter log;
    
    public synchronized void startLogging(@NonNull Context context) {
        if(!logging) {
            Log.i(TAG, "Starting logging");
            logging = true;
            startTimeMillis = System.currentTimeMillis();
            startTimeNano = System.nanoTime();
            stopTimeNano = -1;
            try {
                startFileLogging(context);
                EventBus.getDefault().post(new LoggingEvent());
            } catch(IOException e) {
                Log.e(TAG, "Error starting logging", e);
            }
        } else {
            Log.e(TAG, "startLogging() called when database already logging");
        }
    }

    /**
     * Stop data logging, and return track data
     */
    public synchronized TrackFile stopLogging() {
        if(logging) {
            Log.i(TAG, "Stopping logging");
            final File logFile = stopFileLogging();
            logging = false;
            EventBus.getDefault().post(new LoggingEvent());
            if(logFile != null) {
                return new TrackFile(logFile);
            } else {
                return null;
            }
        } else {
            Log.e(TAG, "stopLogging() called when database isn't logging");
            return null;
        }
    }

    public boolean isLogging() {
        return logging;
    }

    public long getStartTime() {
        if(logging) {
            return startTimeMillis;
        } else {
            return 0;
        }
    }

    /**
     * Returns the amount of time we've been logging, as a nice string 0:00.000
     */
    public String getLogTime() {
        if(logging) {
            long nanoTime;
            if (stopTimeNano == -1) {
                nanoTime = System.nanoTime() - startTimeNano;
            } else {
                nanoTime = stopTimeNano - startTimeNano;
            }
            final long millis = (nanoTime / 1000000L) % 1000;
            final long seconds = (nanoTime / 1000000000L) % 60;
            final long minutes = nanoTime / 60000000000L;
            return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis);
        } else {
            return "";
        }
    }


    private void startFileLogging(@NonNull Context appContext) throws IOException {
        // Open log file for writing
        final File logDir = TrackFiles.getTrackDirectory(appContext);
        final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        final String timestamp = dt.format(new Date());

        // gzip log file
        logFile = new File(logDir, "track_" + timestamp + ".csv.gz");
        // Avoid file conflicts
        for(int i = 2; logFile.exists(); i++) {
            logFile = new File(logDir, "track_" + timestamp + "_" + i + ".csv.gz");
        }
        log = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(logFile))));

        // Write header
        log.write(Measurement.header + "\n");

        // Start sensor updates
        EventBus.getDefault().register(this);
        Services.location.addListener(this);
        Services.sensors.addListener(this);

        Log.i(TAG, "Logging to " + logFile);
    }

    /**
     * Stop logging and return the log file.
     * Precondition: logging = true
     */
    private File stopFileLogging() {
        stopTimeNano = System.nanoTime();

        // Stop sensor updates
        EventBus.getDefault().unregister(this);
        Services.location.removeListener(this);
        Services.sensors.removeListener(this);

        // Close file writer
        try {
            log.close();
            Log.i(TAG, "Logging stopped for " + logFile.getName());
            return logFile;
        } catch (IOException e) {
            Log.e(TAG, "Failed to close log file " + logFile, e);
            FirebaseCrash.report(e);
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
        if(logging) {
            try {
                log.write(line);
                log.write('\n');
            } catch (IOException e) {
                Log.e(TAG, "Failed to write to log file " + logFile, e);
                FirebaseCrash.report(e);
            }
        } else {
            Log.e(TAG, "Attempted to log after closing file");
        }
    }

}
