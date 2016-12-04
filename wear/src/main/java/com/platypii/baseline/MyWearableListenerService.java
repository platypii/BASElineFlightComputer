package com.platypii.baseline;

import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Service to listen for messages from mobile app
 */
public class MyWearableListenerService extends WearableListenerService {
    private static final String TAG = "WearableListenerService";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "Got message: " + messageEvent);
    }

}
