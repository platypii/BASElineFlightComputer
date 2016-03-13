package com.platypii.baseline.audible;

import android.os.Handler;
import android.util.Log;

/**
 * Periodically gives audio feedback
 */
public class AudibleThread {
    private static final String TAG = "AudibleThread";

    private boolean isEnabled = false;

    private final Handler handler = new Handler();

    private final Runnable audibleThread = new Runnable() {
        @Override
        public void run() {
            MyAudible.speak();
            final int delay = MyAudible.getDelay();
            handler.postDelayed(this, delay);
        }
    };

    public void start() {
        if(!isEnabled) {
            Log.i(TAG, "Starting audible");
            final int delay = MyAudible.getDelay();
            handler.postDelayed(audibleThread, delay);
            // handler.post(audibleThread);
            isEnabled = true;
        } else {
            Log.e(TAG, "Failed to start audible: audible thread already started");
        }
    }

    public void stop() {
        if(isEnabled) {
            Log.i(TAG, "Stopping audible");
            handler.removeCallbacks(audibleThread);
            // TODO: Block until stopped?
            isEnabled = false;
        } else {
            Log.e(TAG, "Failed to stop audible: audible thread not started");
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

}
