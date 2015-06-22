package com.platypii.baseline;

import android.content.Context;
import android.content.SharedPreferences;

public class Auth {
    private static final String AUTH_KEY = "is_authenticated";

    public static boolean isAuthenticated(Context context) {
        // Check if user has authenticated
        final SharedPreferences prefs = context.getSharedPreferences("baseline", Context.MODE_PRIVATE);
        return prefs.getBoolean(AUTH_KEY, false);
    }
    public static void setAuthenticated(Context context, boolean auth) {
        // Update preferences
        final SharedPreferences.Editor editor = context.getSharedPreferences("baseline", Context.MODE_PRIVATE).edit();
        editor.putBoolean(Auth.AUTH_KEY, auth);
        editor.apply();
    }

}
