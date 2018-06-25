package com.platypii.baseline.util;

import android.util.Log;
import com.crashlytics.android.Crashlytics;

public class Exceptions {
    private static final String TAG = "Exceptions";

    public static void report(Throwable e) {
        try {
            Log.e(TAG, "Crash report exception", e);
            Crashlytics.logException(e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception while reporting exception", e2);
        }
    }

    public static void log(String msg) {
        try {
            Crashlytics.log(msg);
        } catch (Exception e) {
            Log.e(TAG, "Exception while logging", e);
        }
    }

}
