package com.platypii.baseline.jarvis;

import com.platypii.baseline.Service;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Situational awareness engine
 */
public class FlightComputer implements Service, MyLocationListener {
    private static final String TAG = "FlightComputer";

    private final AutoStop autoStop = new AutoStop();
    private int startCount = 0;

    /**
     * Return a human readable flight mode (
     */
    public String getModeString() {
        if(Services.location.lastLoc != null) {
            final double groundSpeed = Services.location.lastLoc.groundSpeed();
            final double climb = Services.location.lastLoc.climb;
            return FlightMode.getModeString(FlightMode.getMode(groundSpeed, climb));
        } else {
            return FlightMode.getModeString(FlightMode.MODE_UNKNOWN);
        }
    }

    public void start(@NonNull Context context) {
        // Start listening for updates
        Services.location.addListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onLocationChanged(MLocation loc) {
        // Update autostop
        autoStop.update(loc);
    }
    @Override
    public void onLocationChangedPostExecute() {}

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAudibleEvent(AudibleEvent audible) {
        updateAutoStop(audible.started);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLoggingEvent(LoggingEvent logging) {
        updateAutoStop(logging.started);
    }

    /**
     * When audible or logging is started, we should start the autostop session.
     * When both stop, we should stop autostop.
     * Synchronized to prevent racism, and blocking okay because always called in async thread.
     * @param started whether the audible/logger is starting or stopping
     */
    private synchronized void updateAutoStop(boolean started) {
        if(started) {
            startCount++;
            if(startCount == 1) {
                autoStop.start();
            }
        } else {
            startCount--;
            if(startCount < 0) {
                Log.e(TAG, "startCount should never be negative");
            }
            if(startCount == 0) {
                autoStop.stop();
            }
        }
    }

    public void stop() {
        // Stop updates
        Services.location.removeListener(this);
        EventBus.getDefault().unregister(this);
    }

}
