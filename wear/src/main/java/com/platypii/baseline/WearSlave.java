package com.platypii.baseline;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Manages communication with a mobile device
 */
class WearSlave implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WearSlave";

    private static final String WEAR_MSG_RECORD = "BASEline.record";
    private static final String WEAR_MSG_STOP = "BASEline.stop";
    private static final String WEAR_MSG_ENABLE_AUDIBLE = "BASEline.enableAudible";
    private static final String WEAR_MSG_DISABLE_AUDIBLE = "BASEline.disableAudible";

    private GoogleApiClient googleApiClient;
    private boolean connected = false;

    // Only valid while connected:
    private String phoneId;

    WearSlave(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    void clickRecord() {
        if(connected) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_RECORD, null);
        } else {
            Log.w(TAG, "Failed to start recording: wearable not connected to phone");
        }
    }

    void clickStop() {
        if(connected) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_STOP, null);
        } else {
            Log.w(TAG, "Failed to stop recording: wearable not connected to phone");
        }
    }

    void enableAudible() {
        if(connected) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_ENABLE_AUDIBLE, null);
        } else {
            Log.w(TAG, "Failed to enable audible: wearable not connected to phone");
        }
    }

    void disableAudible() {
        if(connected) {
            Wearable.MessageApi.sendMessage(googleApiClient, phoneId, WEAR_MSG_DISABLE_AUDIBLE, null);
        } else {
            Log.w(TAG, "Failed to disable audible: wearable not connected to phone");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connected = true;
        phoneId = getPhoneId();
    }

    /**
     * Get the nodeId of the paired mobile device
     */
    private String getPhoneId() {
        final NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        final List<Node> nodes = result.getNodes();
        if (!nodes.isEmpty()) {
            return nodes.get(0).getId();
        } else {
            return null;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        connected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        connected = false;
    }

    void stop() {
        googleApiClient.disconnect();
        googleApiClient = null;
        connected = false;
    }

}
