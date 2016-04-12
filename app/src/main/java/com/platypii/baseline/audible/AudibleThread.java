package com.platypii.baseline.audible;

import android.os.Handler;
import android.util.Log;

/**
 * Periodically gives audio feedback
 */
class AudibleThread {
    private static final String TAG = "AudibleThread";

    private boolean isRunning = false;

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
        if(!isRunning) {
            Log.i(TAG, "Starting audible");
            final int delay = MyAudible.getDelay();
            handler.postDelayed(audibleThread, delay);
            // handler.post(audibleThread);
            isRunning = true;
        } else {
            Log.e(TAG, "Failed to start audible: audible thread already started");
        }
    }

    public void stop() {
        if(isRunning) {
            Log.i(TAG, "Stopping audible");
            handler.removeCallbacks(audibleThread);
            // TODO: Block until stopped?
            isRunning = false;
        } else {
            Log.e(TAG, "Failed to stop audible: audible thread not started");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

}
