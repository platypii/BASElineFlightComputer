package com.platypii.baseline;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";

    static final String ACTION_START_LOGGING = "start_logging";
    static final String ACTION_START_AUDIBLE = "start_audible";
    static final String ACTION_STOP_LOGGING = "stop_logging";
    static final String ACTION_STOP_AUDIBLE = "stop_audible";
    static final String ACTION_CLICK_STOP = "click_stop";

    private boolean logging = false;
    private boolean audible = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent == null ? null : intent.getAction();
        if (ACTION_START_LOGGING.equals(action)) {
            Log.i(TAG, "Received start logging action");
            logging = true;
            updateNotification();
        } else if (ACTION_START_AUDIBLE.equals(action)) {
            Log.i(TAG, "Received start audible action");
            audible = true;
            updateNotification();
        } else if (ACTION_STOP_LOGGING.equals(action)) {
            Log.i(TAG, "Received stop logging action");
            logging = false;
            updateNotification();
        } else if (ACTION_STOP_AUDIBLE.equals(action)) {
            Log.i(TAG, "Received stop audible action");
            audible = false;
            updateNotification();
        } else if (ACTION_CLICK_STOP.equals(action)) {
            Log.i(TAG, "User clicked notification stop action");
            // Stop audible and logging
            Services.logger.stopLogging();
            Services.audible.disableAudible();
        } else {
            Log.e(TAG, "unexpected action: " + action);
        }
        return START_STICKY;
    }

    private void updateNotification() {
        if (logging || audible) {
            // Show notification
            Log.i(TAG, "Showing notification");
            final Notification notification = Notifications.getNotification(this, logging, audible);
            startForeground(Notifications.notificationId, notification);
        } else {
            // Stop service
            Log.i(TAG, "Stopping foreground service");
            stopForeground(true);
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
