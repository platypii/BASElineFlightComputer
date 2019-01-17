package com.platypii.baseline.cloud;

import com.platypii.baseline.R;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Tools for getting google sign in authentication tokens
 */
public class AuthToken {
    private static final String TAG = "AuthToken";

    /**
     * Get google auth token. Blocking!
     */
    public static @NonNull String getAuthToken(@NonNull Context context) throws AuthException {
        final long startTime = System.currentTimeMillis();
        final GoogleApiClient googleApiClient = getClient(context);
        try {
            final ConnectionResult result = googleApiClient.blockingConnect();
            if (result.isSuccess()) {
                final GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient).await();
                final GoogleSignInAccount account = googleSignInResult.getSignInAccount();
                if (account != null) {
                    final String authToken = account.getIdToken();
                    if (authToken != null) {
                        Log.i(TAG, "Got auth token in " + (System.currentTimeMillis() - startTime) + " ms");
                        return authToken;
                    } else {
                        throw new AuthException("Failed to get auth token");
                    }
                } else {
                    throw new AuthException("Not signed in");
                }
            } else {
                throw new AuthException("Sign in connection error");
            }
        } finally {
            googleApiClient.disconnect();
        }
    }

    private static GoogleApiClient getClient(@NonNull Context context) {
        // Prepare google sign options
        final String serverClientId = context.getString(R.string.server_client_id);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API
        return new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

}
