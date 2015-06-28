package com.platypii.baseline;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
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

public class BaseActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignIn";

    static final String SERVER_CLIENT_ID = "1025187389465-71cuhba0pqbuq2im4ldm1kj710q4oefb.apps.googleusercontent.com";

    /* Client used to interact with Google APIs. */
    GoogleApiClient mGoogleApiClient;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Initialize GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "Connected: " + bundle);
        mShouldResolve = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                Log.e(TAG, "Sign in failed: " + connectionResult);
                // Could not resolve the connection result, return to LoginActivity
                final Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            // Sign in failed, return to LoginActivity
            final Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void getAuthToken(final Callback<String> callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String...params) {
                final String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                final Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                final String scopes = "audience:server:client_id:" + SERVER_CLIENT_ID; // Not the app's client ID
                try {
                    return GoogleAuthUtil.getToken(BaseActivity.this, account, scopes, null);
                } catch(UserRecoverableAuthException e) {
                    Log.e(TAG, "User recoverable auth error", e);
                    startActivity(e.getIntent());
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
                    Log.i(TAG, "Got auth token " + token);
                    if(callback != null) {
                        callback.apply(token);
                    }
                } else {
                    if(callback != null) {
                        callback.error("Failed to get token");
                    }
                }
            }
        }.execute();
    }

}
