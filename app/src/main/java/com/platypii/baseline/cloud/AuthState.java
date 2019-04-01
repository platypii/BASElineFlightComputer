package com.platypii.baseline.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

// Save last sign in state so that sign in panel doesn't blink
public abstract class AuthState {
    private static final String PREF_AUTH_USER = "auth_user";

    public static class SignedOut extends AuthState {
        @NonNull
        @Override
        public String toString() {
            return "SignedOut";
        }
    }
    public static class SigningIn extends AuthState {
        @NonNull
        @Override
        public String toString() {
            return "SigningIn";
        }
    }
    public static class SignedIn extends AuthState {
        final String userId;
        public SignedIn(String userId) {
            this.userId = userId;
        }
        @NonNull
        @Override
        public String toString() {
            return "SignedIn(" + userId + ")";
        }
    }

    @Nullable
    public static AuthState currentAuthState = null;

    /**
     * Load currentAuthState from preferences, if needed
     */
    public static void loadFromPreferences(@NonNull Context context) {
        if (currentAuthState == null) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final String userId = prefs.getString(PREF_AUTH_USER, null);
            if (userId != null) {
                currentAuthState = new SignedIn(userId);
            } else {
                currentAuthState = new SignedOut();
            }
        }
    }

    @Nullable
    public static String getUser() {
        if (currentAuthState instanceof SignedIn) {
            return ((SignedIn) currentAuthState).userId;
        } else {
            return null;
        }
    }

    public static void setState(@NonNull Context context, AuthState state) {
        currentAuthState = state;
        if (currentAuthState instanceof SignedIn) {
            final String userId = ((SignedIn) currentAuthState).userId;
            // Save to preferences
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_AUTH_USER, userId);
            editor.apply();
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AuthState && o.toString().equals(toString());
    }

}
