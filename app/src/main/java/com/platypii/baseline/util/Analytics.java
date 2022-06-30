package com.platypii.baseline.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

    @SuppressLint("MissingPermission")
    public static void logEvent(@Nullable Context context, @NonNull String eventName, @Nullable Bundle bundle) {
        if (context != null) {
            FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle);
        }
    }

}
