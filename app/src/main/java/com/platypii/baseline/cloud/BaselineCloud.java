package com.platypii.baseline.cloud;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaselineCloud {

    public static final String baselineServer = "https://baseline.ws";

    @Nullable
    private ConnectivityManager connectivityManager;

    /**
     * Return true if there is a network connection available
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager != null) {
            final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } else {
            return false;
        }
    }

    public void start(@NonNull Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
