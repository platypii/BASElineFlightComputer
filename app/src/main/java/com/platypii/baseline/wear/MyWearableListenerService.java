package com.platypii.baseline.wear;

import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
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

    private static final String WEAR_PING = "/baseline/ping";

    private static final String WEAR_SERVICE_PREFIX = "/baseline/service";
    private static final String WEAR_SERVICE_OPEN_APP = WEAR_SERVICE_PREFIX + "/openApp";
    private static final String WEAR_SERVICE_PONG = WEAR_SERVICE_PREFIX + "/pong";

    private GoogleApiClient googleApiClient;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Log.d(TAG, "Received message: " + messageEvent);
        switch(messageEvent.getPath()) {
            case WEAR_SERVICE_OPEN_APP:
                // Launch app
                if(!MainActivity.isActive) {
                    Log.i(TAG, "Received app start message, starting app");
                    Intents.openApp(this);
                } else {
                    Log.w(TAG, "Received app start message, but main is already active");
                }
                break;
            case WEAR_PING:
                if(googleApiClient != null && googleApiClient.isConnected()) {
                    Log.d(TAG, "Wear service ping? pong!");
                    final String nodeId = messageEvent.getSourceNodeId();
                    Wearable.MessageApi.sendMessage(googleApiClient, nodeId, WEAR_SERVICE_PONG, null);
                } else {
                    Log.e(TAG, "Unable to respond to service ping");
                }
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }

}
