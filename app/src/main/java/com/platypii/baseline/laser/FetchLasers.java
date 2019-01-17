package com.platypii.baseline.laser;

import com.platypii.baseline.cloud.AuthToken;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.IOException;

public class FetchLasers implements Runnable {
    private static final String TAG = "FetchLasers";

    private final Context context;

    public FetchLasers(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.i(TAG, "Downloading lasers");
        try {
            // Get auth token
            final String authToken = AuthToken.getAuthToken(context);
            // Make HTTP request
            downloadLasers(authToken);
            // TODO: Check file hash?
            Log.i(TAG, "Laser download successful");
        } catch (IOException e) {
            Log.e(TAG, "Failed to download lasers", e);
        }
    }

    /**
     * HTTP get track from baseline
     */
    private void downloadLasers(@NonNull String auth) throws IOException {

    }
}
