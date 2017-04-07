package com.platypii.baseline.jarvis;

import com.platypii.baseline.Service;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.support.annotation.NonNull;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

// TODO: Monitor logging, and perform auto-stop

/**
 * Situational awareness engine
 */
public class FlightComputer implements Service, MyLocationListener {

    private AutoStop autoStop = new AutoStop();

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
    public void onLoggingEvent(LoggingEvent logging) {
        if(logging.started) {
            autoStop.startLogging();
        } else {
            autoStop.stopLogging();
        }
    }

    public void stop() {
        // Stop updates
        Services.location.removeListener(this);
        EventBus.getDefault().unregister(this);
    }

}
