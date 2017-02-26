package com.platypii.baseline;

import com.platypii.baseline.alti.MyAltimeter;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Start and stop essential services
 */
public class Services {
    private static final String TAG = "Services";

    // Count the number of times an activity has started.
    // This allows us to only stop services once the app is really done.
    private static int startCount = 0;
    private static boolean initialized = false;

    // How long to wait after the last activity shutdown to terminate services
    private final static Handler handler = new Handler();
    private static final int shutdownDelay = 10000;

    // Services
    public static final MyAltimeter alti = new MyAltimeter();
    private static final WearSlave wear = new WearSlave();
    static final RemoteApp remoteApp = new RemoteApp(wear);

    public static void start(@NonNull Activity activity) {
        startCount++;
        if(!initialized) {
            initialized = true;
            Log.i(TAG, "Starting services");
            final Context appContext = activity.getApplicationContext();
            handler.removeCallbacks(stopRunnable);

            // Start the various services

            Log.i(TAG, "Starting altimeter");
            alti.start(appContext);

            Log.i(TAG, "Starting wear messaging service");
            wear.start(appContext);
            remoteApp.startPinging();

            Log.i(TAG, "Services started");
        } else {
            Log.v(TAG, "Services already started");
        }
    }

    public static void stop() {
        startCount--;
        if(startCount == 0) {
            handler.postDelayed(stopRunnable, shutdownDelay);
        }
    }

    /**
     * A thread that shuts down services after activity has stopped
     */
    private static final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            if(initialized && startCount == 0) {
                Log.i(TAG, "All activities have stopped. Stopping services.");
                // Stop services
                remoteApp.stopPinging();
                remoteApp.stopService();
                wear.stop();
                alti.stop();
                initialized = false;
            }
        }
    };

}
