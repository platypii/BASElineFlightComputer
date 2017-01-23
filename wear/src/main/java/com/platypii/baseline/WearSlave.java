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
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.platypii.baseline.alti.Convert;
import com.platypii.baseline.events.DataSyncEvent;
import com.platypii.baseline.location.LocationStatus;

import org.greenrobot.eventbus.EventBus;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Manages communication with a mobile device
 */
class WearSlave implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<CapabilityApi.GetCapabilityResult>,
        DataApi.DataListener, MessageApi.MessageListener {
    private static final String TAG = "WearSlave";

    private static final String CAPABILITY_BASELINE_MASTER = "baseline_master";

    private RemoteApp remoteApp;
    private GoogleApiClient googleApiClient;

    // Only valid while connected:
    private String phoneId;

    // Last time we synced with the phone (millis since epoch)
    private long lastPong = 0;

    // According to android docs, messages can be delayed up to 30 minutes.
    // Experimentally, it's instant when phone is active, slow when asleep.
    private static final long connectionTimeout = 120 * 1000; // milliseconds

    WearSlave(@NonNull Context context, RemoteApp remoteApp) {
        Log.i(TAG, "Starting wear messaging service");
        this.remoteApp = remoteApp;
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
        final long lastPingDuration = System.currentTimeMillis() - lastPong; // milliseconds
        if(lastPong == 0) {
            // Mobile device has not yet announced
            return false;
        } else if(lastPingDuration <= connectionTimeout) {
            active = true;
            return true;
        } else {
            if(active) {
                Log.w(TAG, String.format(Locale.getDefault(), "Connection to mobile device timed out, last update %.3s", lastPingDuration * 0.001));
                active = false;
            }
            return false;
        }
    }

    /**
     * Send a message from the wear device to the phone
     */
    void sendMessage(final String message, byte[] data) {
        if(phoneId != null) {
            // Send message to mobile device
            // Log.d(TAG, "Sending " + message + " to " + phoneId);
            final PendingResult<MessageApi.SendMessageResult> result =
                    Wearable.MessageApi.sendMessage(googleApiClient, phoneId, message, data);
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

    private int pingCount = 0;
    /**
     * Ask the mobile device to give us a state update.
     * Same message as data sync, but doesn't sent the synced flag to false.
     */
    void sendPing() {
        Log.d(TAG, "Sending ping " + pingCount);
        final byte[] data = Long.toString(pingCount).getBytes();
        sendMessage(WearMessages.WEAR_PING, data);
        pingCount++;
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
                if (item.getUri().getPath().equals(WearMessages.STATE_URI)) {
                    final DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    // Get metric/imperial units from mobile device settings
                    Convert.metric = dataMap.getBoolean("metric");
                    // Update logging and audible state
                    final boolean logging = dataMap.getBoolean("logging_enabled");
                    final boolean audible = dataMap.getBoolean("audible_enabled");
                    final String message = dataMap.getString("gps_status_message");
                    final int iconColor = dataMap.getInt("gps_status_color");
                    final LocationStatus locationStatus = new LocationStatus(message, iconColor);
                    remoteApp.onSync(logging, audible, locationStatus);
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
            case WearMessages.WEAR_SERVICE_PONG:
                final byte[] data = messageEvent.getData();
                Log.d(TAG, "Received pong " + new String(data));
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
        Wearable.CapabilityApi.getCapability(googleApiClient, CAPABILITY_BASELINE_MASTER, CapabilityApi.FILTER_REACHABLE)
                .setResultCallback(this);
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
    }

    /**
     * Get the nodeId of the paired mobile device
     */
    @Override
    public void onResult(@NonNull CapabilityApi.GetCapabilityResult result) {
        final CapabilityInfo capability = result.getCapability();
        final Set<Node> nodes = capability.getNodes();
        if(nodes.isEmpty()) {
            Log.w(TAG, "No connected devices found");
        } else {
            // At least one node found, return the first
            final Iterator<Node> it = nodes.iterator();
            phoneId = it.next().getId();
            if(it.hasNext()) {
                Log.w(TAG, "More than one connected device found");
            }
        }
        if(phoneId != null) {
            // Request initial data sync
            sendMessage(WearMessages.WEAR_APP_INIT, null);
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
        EventBus.getDefault().post(new DataSyncEvent());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Wear connection failed: " + connectionResult);
        EventBus.getDefault().post(new DataSyncEvent());
    }

    void stop() {
        Log.i(TAG, "Stopping wear messaging service");
        googleApiClient.disconnect();
        EventBus.getDefault().post(new DataSyncEvent());
    }

}
