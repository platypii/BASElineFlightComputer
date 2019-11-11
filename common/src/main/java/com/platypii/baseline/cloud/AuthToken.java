package com.platypii.baseline.cloud;

import com.platypii.baseline.common.R;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.concurrent.ExecutionException;

/**
 * Tools for getting google sign in authentication tokens
 */
public class AuthToken {
    private static final String TAG = "AuthToken";

    /**
     * Get google auth token. Blocking!
     */
    @NonNull
    public static String getAuthToken(@NonNull Context context) throws AuthException {
        final long startTime = System.currentTimeMillis();
        final GoogleSignInClient signInClient = getClient(context);
        final Task<GoogleSignInAccount> futureAccount = signInClient.silentSignIn();
        try {
            final GoogleSignInAccount account = Tasks.await(futureAccount);
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
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof ApiException) {
                final ApiException apiException = (ApiException) cause;
                final String statusString = CommonStatusCodes.getStatusCodeString(apiException.getStatusCode());
                throw new AuthException("Sign in failed " + statusString, apiException);
            } else {
                throw new AuthException("Sign in failed", e);
            }
        } catch (InterruptedException e) {
            throw new AuthException("Sign in interrupted", e);
        }
    }

    @NonNull
    private static GoogleSignInClient getClient(@NonNull Context context) {
        // Prepare google sign options
        final String serverClientId = context.getString(R.string.server_client_id);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with access to the Google Sign-In API
        return GoogleSignIn.getClient(context, gso);
    }

}
