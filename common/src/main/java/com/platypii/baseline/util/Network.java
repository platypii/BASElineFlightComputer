package com.platypii.baseline.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Network utilities
 */
public class Network {

    /**
     * Return true if there is a network connection available
     */
    public static boolean isAvailable(ConnectivityManager connectivityManager) {
        if (connectivityManager != null) {
            final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } else {
            return false;
        }
    }

}
