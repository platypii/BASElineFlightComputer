package com.platypii.baseline;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.platypii.baseline.alti.Convert;
import com.platypii.baseline.events.DataSyncEvent;

import org.greenrobot.eventbus.EventBus;
import java.util.List;

/**
 * Manages communication with a mobile device
 */
class WearSlave implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<NodeApi.GetConnectedNodesResult>, DataApi.DataListener, MessageApi.MessageListener {
    private static final String TAG = "WearSlave";

    private static final String STATE_URI = "/baseline/services/state";

    private static final String WEAR_MSG_INIT = "/baseline/init";
    private static final String WEAR_MSG_PING = "/baseline/ping";
    private static final String WEAR_MSG_PONG = "/baseline/pong";
    private static final String WEAR_MSG_RECORD = "/baseline/record";
    private static final String WEAR_MSG_STOP = "/baseline/stop";
    private static final String WEAR_MSG_ENABLE_AUDIBLE = "/baseline/enableAudible";
    private static final String WEAR_MSG_DISABLE_AUDIBLE = "/baseline/disableAudible";
    private static final String WEAR_MSG_OPEN_APP = "/baseline/openApp";

    private GoogleApiClient googleApiClient;

    // Only valid while connected:
    private String phoneId;

    // App state synced from phone
    boolean synced = false;
    boolean logging = false;
    boolean audible = false;

    // Last time we synced with the phone (millis since epoch)
    private long lastPong = 0;
    private static final long connectionTimeout = 3000; // milliseconds

    WearSlave(@NonNull Context context) {
        Log.i(TAG, "Starting wear messaging service");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    boolean isConnected() {
        return googleApiClient.isConnected() && phoneId != null;
    }

    private boolean active = false;
    boolean isActive() {
        if(System.currentTimeMillis() - connectionTimeout <= lastPong) {
            active = true;
            return true;
        } else if(lastPong == 0) {
            // Log.d(TAG, "Mobile device has not yet announced");
            return false;
        } else {
            if(active) {
                Log.w(TAG, "Connection to mobile device timed out");
                active = false;
            }
            return false;
        }
    }

    private void sendMessage(final String message) {
        if(phoneId != null) {
            // Send message to mobile device
            // Log.d(TAG, "Sending " + message + " to " + phoneId);
            final PendingResult<MessageApi.SendMessageResult> result =
                    Wearable.MessageApi.sendMessage(googleApiClient, phoneId, message, null);
            // Handle result
            result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                    // Log.d(TAG, "Message result: " + sendMessageResult.getStatus());
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        Log.w(TAG, "Message error: " + sendMessageResult.getStatus());
                    }
                }
            });
        } else {
            Log.e(TAG, "Message error: phone id not available to send " + message);
        }
    }

    /**
     * Ask the mobile device to give us a state update
     */
    void requestDataSync() {
        Log.i(TAG, "Requesting data sync");
        synced = false;
        EventBus.getDefault().post(new DataSyncEvent());
        sendMessage(WEAR_MSG_INIT);
    }

    /**
     * Ask the mobile device to give us a state update.
     * Same message as data sync, but doesn't sent the synced flag to false.
     */
    void sendPing() {
        // Log.d(TAG, "Sending ping");
        sendMessage(WEAR_MSG_PING);
    }

    void clickRecord() {
        if(isConnected()) {
            sendMessage(WEAR_MSG_RECORD);
            logging = true;
            synced = false;
        } else {
            Log.w(TAG, "Failed to start recording: wearable not connected to phone");
        }
    }

    void clickStop() {
        if(isConnected()) {
            sendMessage(WEAR_MSG_STOP);
            logging = false;
            synced = false;
        } else {
            Log.w(TAG, "Failed to stop recording: wearable not connected to phone");
        }
    }

    void enableAudible() {
        if(isConnected()) {
            sendMessage(WEAR_MSG_ENABLE_AUDIBLE);
            audible = true;
            synced = false;
        } else {
            Log.w(TAG, "Failed to enable audible: wearable not connected to phone");
        }
    }

    void disableAudible() {
        if(isConnected()) {
            sendMessage(WEAR_MSG_DISABLE_AUDIBLE);
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
        sendMessage(WEAR_MSG_OPEN_APP);
    }

    /**
     * Called when there is new app state on the phone
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "Received sync data from phone: " + dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                final DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals(STATE_URI)) {
                    final DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    // Update logging and audible state
                    logging = dataMap.getBoolean("logging_enabled");
                    audible = dataMap.getBoolean("audible_enabled");
                    Convert.metric = dataMap.getBoolean("metric");
                    synced = true;
                    // Data sync counts as a heartbeat from the phone:
                    lastPong = System.currentTimeMillis();
                    EventBus.getDefault().post(new DataSyncEvent());
                    Log.i(TAG, "Received sync data from phone: logging = " + logging + " audible = " + audible);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
                Log.d(TAG, "Received data delete: " + event);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Log.d(TAG, "Received message: " + messageEvent);
        switch (messageEvent.getPath()) {
            case WEAR_MSG_PONG:
                // Log.d(TAG, "Received pong");
                lastPong = System.currentTimeMillis();
                break;
            default:
                Log.w(TAG, "Received unknown message: " + messageEvent.getPath());
        }
    }

    // Google api client callbacks
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "Google api connected");
        // Start fetching list of wear nodes
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(this);
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
    }

    /**
     * Get the nodeId of the paired mobile device
     */
    @Override
    public void onResult(@NonNull NodeApi.GetConnectedNodesResult result) {
        final List<Node> nodes = result.getNodes();
        switch(nodes.size()) {
            case 0:
                Log.w(TAG, "No connected devices found");
                phoneId = null;
                break;
            case 1:
                phoneId = nodes.get(0).getId();
                break;
            default:
                Log.w(TAG, "More than one connected device found");
                phoneId = nodes.get(0).getId();
        }
        if(phoneId != null) {
            requestDataSync();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if(cause == CAUSE_NETWORK_LOST) {
            Log.w(TAG, "Wear connection suspended: device connection lost");
        } else if(cause == CAUSE_SERVICE_DISCONNECTED) {
            Log.w(TAG, "Wear connection suspended: service killed");
        } else {
            Log.e(TAG, "Wear connection suspended: unknown reason");
        }
        synced = false;
        EventBus.getDefault().post(new DataSyncEvent());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Wear connection failed: " + connectionResult);
        synced = false;
        EventBus.getDefault().post(new DataSyncEvent());
    }

    private Thread pingThread;
    private final PingRunnable pingRunnable = new PingRunnable(this);
    void startPingThread() {
        if(pingThread == null) {
            Log.i(TAG, "Starting ping thread");
            pingThread = new Thread(pingRunnable);
            pingThread.start();
        } else {
            Log.e(TAG, "startPingThread called twice");
        }
    }

    void stopPingThread() {
        Log.i(TAG, "Stopping ping thread");
        pingRunnable.stop();
        pingThread = null;
    }

    void stop() {
        Log.i(TAG, "Stopping wear messaging service");
        googleApiClient.disconnect();
        synced = false;
        EventBus.getDefault().post(new DataSyncEvent());
    }

}
