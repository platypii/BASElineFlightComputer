package com.platypii.baseline;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

public class GoogleAuth {
    private static final String TAG = "GoogleAuth";

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    private static final String SERVER_CLIENT_ID = "1025187389465-71cuhba0pqbuq2im4ldm1kj710q4oefb.apps.googleusercontent.com";

    private static GoogleApiClient googleApiClient = null;
    private static boolean inProgress = false;

    public static void signin(final Activity context, final Callback<String> callback) {
        Log.i(TAG, "Google sign in");
        if(googleApiClient == null) {
            googleApiClient = initClient(context, callback);
        }
        googleApiClient.connect();
    }

    /**
     * Sign out of google auth
     * @return true iff signed out successfully
     */
    public static boolean signout(Activity context) {
        if(Auth.getAuth(context) == null) {
            Log.e(TAG, "Sign out called but not signed in");
            return false;
        } else {
            if(googleApiClient == null) {
                googleApiClient = initClient(context, null);
            }
            Log.i(TAG, "Sign out");
            googleApiClient.disconnect();
            Auth.setAuth(context, null);
            return true;
        }
    }

    private static GoogleApiClient initClient(final Activity context, final Callback<String> callback) {
        return new GoogleApiClient.Builder(context)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "Authentication successful");
                        // Get google user id
                        getAuthToken(context, callback); // TODO: Don't call during signout
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.e(TAG, "Google API connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        resolveSignInError(context, connectionResult);
                    }
                })
                .build();
    }

    /** Resolve the current ConnectionResult error (ask user for auth) */
    private static void resolveSignInError(Activity context, ConnectionResult connectionResult) {
        Log.i(TAG, "Sign in required - " + connectionResult);
        if(connectionResult.hasResolution()) {
            if(inProgress) {
                Log.w(TAG, "Sign in already in progress");
            }
            try {
                inProgress = true;
                Log.d(TAG, "Resolving sign in error");
                connectionResult.startResolutionForResult(context, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent. Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                inProgress = false;
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Sign in impossible");
        }
    }

    private static void getAuthToken(final Context context, final Callback<String> callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String...params) {
                final String accountName = Plus.AccountApi.getAccountName(googleApiClient);
                final Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                String scopes = "audience:server:client_id:" + SERVER_CLIENT_ID; // Not the app's client ID
                // scopes += " oauth2:profile";
                try {
                    return GoogleAuthUtil.getToken(context, account, scopes, null);
                } catch(UserRecoverableAuthException e) {
                    Log.e(TAG, "User recoverable auth error", e);
                    context.startActivity(e.getIntent());
                    return null;
                } catch(GoogleAuthException e) {
                    Log.e(TAG, "Error retrieving ID token", e);
                    return null;
                } catch(IOException e) {
                    Log.e(TAG, "Error retrieving ID token", e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String token) {
                if(token != null) {
                    Log.i(TAG, "User signed in " + token);
                    Auth.setAuth(context, token);
                    if(callback != null) {
                        // Callback to indicate success
                        callback.apply(token);
                    }
                } else {
                    Toast.makeText(context, "Sign in failed", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

}
