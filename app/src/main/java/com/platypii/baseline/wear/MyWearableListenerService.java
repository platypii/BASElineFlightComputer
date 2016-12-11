package com.platypii.baseline.wear;

import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.platypii.baseline.Intents;
import com.platypii.baseline.MainActivity;

/**
 * Service to listen for messages from mobile app
 */
public class MyWearableListenerService extends WearableListenerService {
    private static final String TAG = "WearableListenerService";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "Received message: " + messageEvent);
        switch(messageEvent.getPath()) {
            case WearMaster.WEAR_MSG_OPEN_APP:
                // Launch app
                if(!MainActivity.isActive) {
                    Log.i(TAG, "Received app start message, starting app");
                    Intents.openApp(this);
                } else {
                    Log.d(TAG, "Received app start message, but main is already active");
                }
                break;
        }
    }

}
