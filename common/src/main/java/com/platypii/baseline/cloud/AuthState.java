package com.platypii.baseline.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;

public abstract class AuthState {
    private static final String PREF_AUTH_USER = "auth_user";
    private static final String PREF_AUTH_TOKEN = "auth_token";

    public static class SignedOut extends AuthState {
        @NonNull
        @Override
        public String toString() {
            return "SignedOut";
        }
    }

    public static class SignedIn extends AuthState {
        @NonNull
        final String userId;
        @NonNull
        final String token;

        public SignedIn(@NonNull String userId, @NonNull String token) {
            this.userId = userId;
            this.token = token;
        }

        @NonNull
        @Override
        public String toString() {
            return "SignedIn(" + userId + ")";
        }
    }

    // Save last sign in state so that sign in panel doesn't blink
    @Nullable
    public static AuthState currentAuthState = null;

    public static boolean signingIn = false;

    /**
     * Load currentAuthState from preferences, if needed
     */
    public static void loadFromPreferences(@NonNull SharedPreferences prefs) {
        if (currentAuthState == null) {
            final String userId = prefs.getString(PREF_AUTH_USER, null);
            final String token = prefs.getString(PREF_AUTH_TOKEN, null);
            if (userId != null && token != null) {
                currentAuthState = new SignedIn(userId, token);
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

    @Nullable
    public static String getToken() {
        if (currentAuthState instanceof SignedIn) {
            return ((SignedIn) currentAuthState).token;
        } else {
            return null;
        }
    }

    /**
     * Call this frequently, but will only emit on state changes
     */
    public static void setState(@NonNull Context context, @NonNull AuthState state) {
        currentAuthState = state;
        String userId = null;
        String token = null;
        // Pattern matching in java... sigh
        if (currentAuthState instanceof SignedIn) {
            final SignedIn signedIn = (SignedIn) currentAuthState;
            userId = signedIn.userId;
            token = signedIn.token;
        }
        // Save to preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_AUTH_USER, userId);
        editor.putString(PREF_AUTH_TOKEN, token);
        editor.apply();
        // Notify listeners
        EventBus.getDefault().post(state);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AuthState && obj.toString().equals(toString());
    }

}
