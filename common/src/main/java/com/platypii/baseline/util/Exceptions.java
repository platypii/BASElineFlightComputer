package com.platypii.baseline.util;

import android.util.Log;
import androidx.annotation.NonNull;
import com.crashlytics.android.Crashlytics;

public class Exceptions {
    private static final String TAG = "Exceptions";

    public static void report(@NonNull Throwable e) {
        try {
            Log.e(TAG, "Crash report exception", e);
            Crashlytics.getInstance().logException(e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception while reporting exception", e2);
        }
    }

    public static void log(@NonNull String msg) {
        try {
            Crashlytics.getInstance().log(msg);
        } catch (Exception e) {
            Log.e(TAG, "Exception while logging", e);
        }
    }

}
