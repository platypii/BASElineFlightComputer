package com.platypii.baseline.wear;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.platypii.baseline.Intents;
import com.platypii.baseline.MainActivity;

/**
 * Service to listen for messages from mobile app
 */
public class MyWearableListenerService extends WearableListenerService {
    private static final String TAG = "WearableListenerService";

    private GoogleApiClient googleApiClient;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Log.d(TAG, "Received message: " + messageEvent);
        switch(messageEvent.getPath()) {
            case WearMessages.WEAR_SERVICE_OPEN_APP:
                // Launch app
                if(!MainActivity.isActive) {
                    Log.i(TAG, "Received app start message, starting app");
                    Intents.openApp(this);
                } else {
                    Log.w(TAG, "Received app start message, but main is already active");
                }
                break;
            case WearMessages.WEAR_PING:
                final byte[] data = messageEvent.getData();
                Log.d(TAG, "Received ping " + new String(data));
                // Send service pong
                sendMessage(messageEvent.getSourceNodeId(), WearMessages.WEAR_SERVICE_PONG, data);
                break;
        }
    }

    private void sendMessage(String nodeId, String message, byte[] data) {
        if(googleApiClient != null) {
            if(!googleApiClient.isConnected()) {
                // Attempt to reconnect
                googleApiClient.blockingConnect();
            }
            if(googleApiClient.isConnected()) {
                Log.d(TAG, "Sending " + message);
                final PendingResult<MessageApi.SendMessageResult> result =
                        Wearable.MessageApi.sendMessage(googleApiClient, nodeId, message, data);
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
            }
        } else {
            Log.e(TAG, "Unable to respond to service ping");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }

}
