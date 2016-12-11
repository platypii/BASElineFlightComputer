package com.platypii.baseline;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.platypii.baseline.events.DataSyncEvent;

import org.greenrobot.eventbus.EventBus;
import java.util.List;

/**
 * Manages communication with a mobile device
 */
class WearSlave implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<NodeApi.GetConnectedNodesResult>, DataApi.DataListener {
    private static final String TAG = "WearSlave";

    private static final String STATE_URI = "/baseline/services/state";

    private static final String WEAR_MSG_INIT = "BASEline.init";
    private static final String WEAR_MSG_RECORD = "BASEline.record";
    private static final String WEAR_MSG_STOP = "BASEline.stop";
    private static final String WEAR_MSG_ENABLE_AUDIBLE = "BASEline.enableAudible";
    private static final String WEAR_MSG_DISABLE_AUDIBLE = "BASEline.disableAudible";

    private GoogleApiClient googleApiClient;

    // Only valid while connected:
    private String phoneId;

    // App state synced from phone
    boolean synced = false;
    boolean logging = false;
    boolean audible = false;

    WearSlave(Context context) {
        Log.i(TAG, "Starting wear messaging service");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    private boolean isConnected() {
        return googleApiClient.isConnected() && phoneId != null;
    }

    /**
     * Ask the mobile device to give us a state update
     */
    private void requestDataSync() {
        Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_INIT, null);
    }

    void clickRecord() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_RECORD, null);
            logging = true;
            synced = false;
        } else {
            Log.w(TAG, "Failed to start recording: wearable not connected to phone");
        }
    }

    void clickStop() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_STOP, null);
            logging = false;
            synced = false;
        } else {
            Log.w(TAG, "Failed to stop recording: wearable not connected to phone");
        }
    }

    void enableAudible() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_ENABLE_AUDIBLE, null);
            audible = true;
            synced = false;
        } else {
            Log.w(TAG, "Failed to enable audible: wearable not connected to phone");
        }
    }

    void disableAudible() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_DISABLE_AUDIBLE, null);
            audible = false;
            synced = false;
        } else {
            Log.w(TAG, "Failed to disable audible: wearable not connected to phone");
        }
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
                    synced = true;
                    EventBus.getDefault().post(new DataSyncEvent());
                    Log.i(TAG, "Received sync data from phone: logging = " + logging + " audible = " + audible);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    // Google api client callbacks
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "Wear connected");
        // Start fetching list of wear nodes
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(this);
        Wearable.DataApi.addListener(googleApiClient, this);
        requestDataSync();
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
                break;
            case 1:
                phoneId = nodes.get(0).getId();
                break;
            default:
                Log.w(TAG, "More than one connected device found");
                phoneId = nodes.get(0).getId();
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

    void stop() {
        Log.w(TAG, "Stopping wear messaging service");
        googleApiClient.disconnect();
        synced = false;
        EventBus.getDefault().post(new DataSyncEvent());
    }

}
