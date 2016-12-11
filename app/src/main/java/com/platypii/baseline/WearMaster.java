package com.platypii.baseline;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Manages communication with a wear device
 */
class WearMaster implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WearMaster";

    private static final String WEAR_MSG_RECORD = "BASEline.record";
    private static final String WEAR_MSG_STOP = "BASEline.stop";
    private static final String WEAR_MSG_ENABLE_AUDIBLE = "BASEline.enableAudible";
    private static final String WEAR_MSG_DISABLE_AUDIBLE = "BASEline.disableAudible";

    private GoogleApiClient googleApiClient;

    WearMaster(Context appContext) {
        Log.i(TAG, "Starting wear messaging service");
        googleApiClient = new GoogleApiClient.Builder(appContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "Received message: " + messageEvent);
        switch(messageEvent.getPath()) {
            case WEAR_MSG_RECORD:
                Log.i(TAG, "Received record message");
                if(!Services.logger.isLogging()) {
                    Services.logger.startLogging();
                } else {
                    Log.w(TAG, "Received record message, but already recording");
                }
                break;
            case WEAR_MSG_STOP:
                Log.i(TAG, "Received stop message");
                if(Services.logger.isLogging()) {
                    Services.logger.stopLogging();
                } else {
                    Log.w(TAG, "Received record message, but already recording");
                }
                break;
            case WEAR_MSG_ENABLE_AUDIBLE:
                Log.i(TAG, "Received audible enable message");
                if(!Services.audible.isEnabled()) {
                    Services.audible.enableAudible();
                } else {
                    Log.w(TAG, "Received audible enable message, but audible already on");
                }
                break;
            case WEAR_MSG_DISABLE_AUDIBLE:
                Log.i(TAG, "Received audible disable message");
                if(Services.audible.isEnabled()) {
                    Services.audible.disableAudible();
                } else {
                    Log.w(TAG, "Received audible disable message, but audible already off");
                }
                break;
            default:
                Log.w(TAG, "Received unknown message: " + messageEvent.getPath());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "Wear connected");
        Wearable.MessageApi.addListener(googleApiClient, this);
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
        // TODO: update();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudibleEvent(AudibleEvent event) {
        // TODO: update();
    }


    void stop() {
        Log.w(TAG, "Stopping wear messaging service");
        EventBus.getDefault().unregister(this);
        Wearable.MessageApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
    }

}
