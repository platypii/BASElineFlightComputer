package com.platypii.baseline.location;

import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.support.annotation.NonNull;
import java.util.List;

class LocationProviderReplay extends LocationProvider {
    private static final String TAG = "LocationProviderReplay";

    private List<MLocation> trackData;

    @Override
    protected String providerName() {
        return TAG;
    }

    @Override
    public void start(@NonNull Context context) {}

    public void loadTrack(List<MLocation> trackData) {
        this.trackData = trackData;
        // Start playback thread
        new Thread(replayThread).start();
    }

    private Runnable replayThread = new Runnable() {
        @Override
        public void run() {
            if(trackData == null || trackData.isEmpty()) return;
            final long jumpTime = trackData.get(0).millis;
            final long startTime = System.currentTimeMillis();
            int index = 0;
            while(index < trackData.size()) {
                final MLocation loc = trackData.get(index);
                final long delay = (loc.millis - jumpTime) - (System.currentTimeMillis() - startTime);
                if(delay > 0) {
                    try {
                        Thread.sleep(delay);
                        // TODO: Publish measurement
                        updateLocation(loc);
                        index++;
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    };
}
