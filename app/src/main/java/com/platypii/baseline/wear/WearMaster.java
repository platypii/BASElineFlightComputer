package com.platypii.baseline.wear;

import com.platypii.baseline.Service;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.location.LocationStatus;
import com.platypii.baseline.util.Convert;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Manages communication with a wear device
 */
public class WearMaster implements Service, MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WearMaster";

    private GoogleApiClient googleApiClient;

    @Override
    public void start(@NonNull Context context) {
        Log.i(TAG, "Starting wear messaging service");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Log.d(TAG, "Received message: " + messageEvent);
        switch (messageEvent.getPath()) {
            case WearMessages.WEAR_APP_INIT:
                Log.i(TAG, "Received hello message");
                sendUpdate();
                break;
            case WearMessages.WEAR_PING:
                Log.i(TAG, "Received ping message");
                sendUpdate();
                break;
            case WearMessages.WEAR_APP_RECORD:
                Log.i(TAG, "Received record message");
                if (!Services.logger.isLogging()) {
                    Services.logger.startLogging();
                } else {
                    Log.w(TAG, "Received record message, but already recording");
                }
                break;
            case WearMessages.WEAR_APP_STOP:
                Log.i(TAG, "Received stop message");
                if (Services.logger.isLogging()) {
                    Services.logger.stopLogging();
                } else {
                    Log.w(TAG, "Received record message, but already recording");
                }
                break;
            case WearMessages.WEAR_APP_AUDIBLE_ENABLE:
                Log.i(TAG, "Received audible enable message");
                if (!Services.audible.isEnabled()) {
                    Services.audible.enableAudible();
                } else {
                    Log.w(TAG, "Received audible enable message, but audible already on");
                }
                break;
            case WearMessages.WEAR_APP_AUDIBLE_DISABLE:
                Log.i(TAG, "Received audible disable message");
                if (Services.audible.isEnabled()) {
                    Services.audible.disableAudible();
                } else {
                    Log.w(TAG, "Received audible disable message, but audible already off");
                }
                break;
            default:
                if(messageEvent.getPath().startsWith(WearMessages.WEAR_APP_PREFIX)) {
                    Log.e(TAG, "Received unknown message: " + messageEvent.getPath());
                }
        }
    }

    private int count = 0;
    public void sendUpdate() {
        Log.i(TAG, "Sending state update to wear device");
        final PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WearMessages.STATE_URI).setUrgent();
        final DataMap map = putDataMapReq.getDataMap();
        map.putBoolean("logging_enabled", Services.logger.isLogging());
        map.putBoolean("audible_enabled", Services.audible.isEnabled());
        map.putBoolean("metric", Convert.metric);
        map.putString("gps_status_message", LocationStatus.getStatus().message);
        map.putInt("gps_status_color", LocationStatus.getStatus().iconColor);
        map.putInt("nonce", count++); // add a unique nonce to force sync TODO: use datamap properly
        final PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        final PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
        // TODO: Check result
    }

    // Google api client callbacks
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "Wear connected");
        Wearable.MessageApi.addListener(googleApiClient, this);
        sendUpdate();
        // TODO: Firebase event for wear connected
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoggingEvent(LoggingEvent event) {
        sendUpdate();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudibleEvent(AudibleEvent event) {
        sendUpdate();
    }

    @Override
    public void stop() {
        Log.i(TAG, "Stopping wear messaging service");
        EventBus.getDefault().unregister(this);
        Wearable.MessageApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
        googleApiClient = null;
    }

}