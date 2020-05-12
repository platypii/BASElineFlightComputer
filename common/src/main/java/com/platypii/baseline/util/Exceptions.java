package com.platypii.baseline.util;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class Exceptions {
    private static final String TAG = "Exceptions";

    public static void report(@NonNull Throwable e) {
        try {
            Log.e(TAG, "Crash report exception", e);
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception e2) {
            Log.e(TAG, "Exception while reporting exception", e2);
        }
    }

    public static void log(@NonNull String msg) {
        try {
            FirebaseCrashlytics.getInstance().log(msg);
        } catch (Exception e) {
            Log.e(TAG, "Exception while logging", e);
        }
    }

}
