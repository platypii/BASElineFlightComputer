package com.platypii.baseline.util;

import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;

public class Exceptions {

    public static void report(Throwable e) {
        try {
            FirebaseCrash.report(e);
        } catch(Exception e2) {
            Log.e("Exceptions", "Exception while reporting exception", e2);
        }
    }

}
