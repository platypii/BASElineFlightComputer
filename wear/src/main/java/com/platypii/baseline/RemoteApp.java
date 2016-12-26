package com.platypii.baseline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.platypii.baseline.events.DataSyncEvent;
import org.greenrobot.eventbus.EventBus;

/**
 * Class for interacting with the baseline mobile app remotely
 */
class RemoteApp {
    private static final String TAG = "RemoteApp";

    private final WearSlave wear;

    // Ping thread to determine connectedness to mobile app
    private PingRunnable pingRunnable;
    private boolean pinging = false;

    // App state synced from phone
    boolean synced = false;
    boolean logging = false;
    boolean audible = false;

    RemoteApp(@NonNull Context context) {
        wear = new WearSlave(context, this);
    }

    /**
     * Ask the mobile device to give us a state update
     */
    void requestDataSync() {
        Log.i(TAG, "Requesting data sync");
        synced = false;
        EventBus.getDefault().post(new DataSyncEvent());
        wear.sendMessage(WearMessages.WEAR_APP_INIT);
    }

    void clickRecord() {
        if(wear.isConnected()) {
            wear.sendMessage(WearMessages.WEAR_APP_RECORD);
            logging = true;
            synced = false;
        } else {
            Log.w(TAG, "Failed to start recording: wearable not connected to phone");
        }
    }

    void clickStop() {
        if(wear.isConnected()) {
            wear.sendMessage(WearMessages.WEAR_APP_STOP);
            logging = false;
            synced = false;
        } else {
            Log.w(TAG, "Failed to stop recording: wearable not connected to phone");
        }
    }

    void enableAudible() {
        if(wear.isConnected()) {
            wear.sendMessage(WearMessages.WEAR_APP_AUDIBLE_ENABLE);
            audible = true;
            synced = false;
        } else {
            Log.w(TAG, "Failed to enable audible: wearable not connected to phone");
        }
    }

    void disableAudible() {
        if(wear.isConnected()) {
            wear.sendMessage(WearMessages.WEAR_APP_AUDIBLE_DISABLE);
            audible = false;
            synced = false;
        } else {
            Log.w(TAG, "Failed to disable audible: wearable not connected to phone");
        }
    }

    /**
     * Start the phone app on a paired device, so that we can start logging or audible remotely
     */
    void startApp() {
        // Send message to mobile device
        wear.sendMessage(WearMessages.WEAR_SERVICE_OPEN_APP);
    }

    /**
     * Called by wear slave when new data sync comes in
     */
    void onSync(boolean logging, boolean audible) {
        this.logging = logging;
        this.audible = audible;
        synced = true;
    }

    boolean isActive() {
        return wear.isActive();
    }

    void startPinging() {
        if(!pinging) {
            Log.i(TAG, "Starting ping thread");
            pinging = true;
            pingRunnable = new PingRunnable(this.wear);
            final Thread pingThread = new Thread(pingRunnable);
            pingThread.start();
        } else {
            Log.e(TAG, "startPinging called when already pinging");
        }
    }

    void stopPinging() {
        Log.i(TAG, "Stopping ping thread");
        if(pinging) {
            pingRunnable.stop();
            pingRunnable = null;
            pinging = false;
        } else {
            Log.e(TAG, "stopPinging called when not pinging");
        }
    }

    /**
     * Stop remote service (don't stop the actual app)
     */
    void stopService() {
        stopPinging();
        wear.stop();
        synced = false;
        EventBus.getDefault().post(new DataSyncEvent());
    }

}
