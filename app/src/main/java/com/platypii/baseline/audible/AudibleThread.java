package com.platypii.baseline.audible;

import android.os.Handler;
import android.util.Log;

/**
 * Periodically gives audio feedback
 */
public class AudibleThread {
    private static final String TAG = "AudibleThread";

    private boolean isEnabled = false;

    private Handler handler = new Handler();
    private int delay; // milliseconds

    private final Runnable audibleThread = new Runnable() {
        @Override
        public void run() {
            MyAudible.playAudio();
            handler.postDelayed(this, delay);
        }
    };

    public AudibleThread(int delay) {
        this.delay = delay;
    }

    public void start() {
        if(!isEnabled) {
            Log.i(TAG, "Starting audible");
            handler.post(audibleThread);
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

}
