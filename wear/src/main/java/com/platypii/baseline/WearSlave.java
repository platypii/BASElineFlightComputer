package com.platypii.baseline;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Manages communication with a mobile device
 */
class WearSlave implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<NodeApi.GetConnectedNodesResult> {
    private static final String TAG = "WearSlave";

    private static final String WEAR_MSG_RECORD = "BASEline.record";
    private static final String WEAR_MSG_STOP = "BASEline.stop";
    private static final String WEAR_MSG_ENABLE_AUDIBLE = "BASEline.enableAudible";
    private static final String WEAR_MSG_DISABLE_AUDIBLE = "BASEline.disableAudible";

    private GoogleApiClient googleApiClient;

    // Only valid while connected:
    private String phoneId;

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

    void clickRecord() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_RECORD, null);
        } else {
            Log.w(TAG, "Failed to start recording: wearable not connected to phone");
        }
    }

    void clickStop() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_STOP, null);
        } else {
            Log.w(TAG, "Failed to stop recording: wearable not connected to phone");
        }
    }

    void enableAudible() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_ENABLE_AUDIBLE, null);
        } else {
            Log.w(TAG, "Failed to enable audible: wearable not connected to phone");
        }
    }

    void disableAudible() {
        if(isConnected()) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_DISABLE_AUDIBLE, null);
        } else {
            Log.w(TAG, "Failed to disable audible: wearable not connected to phone");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "Wear connected");
        // Start fetching list of wear nodes
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(this);
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Wear connection failed: " + connectionResult);
    }

    void stop() {
        Log.w(TAG, "Stopping wear messaging service");
        googleApiClient.disconnect();
    }

}
