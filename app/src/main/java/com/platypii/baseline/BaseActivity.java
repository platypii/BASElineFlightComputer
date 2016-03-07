package com.platypii.baseline;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class BaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BaseActivity";

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount account;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    private boolean userClickedSignIn = false;

    protected boolean isSignedIn() {
        return account != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String serverClientId = getString(R.string.server_client_id);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }

        // Listen for sign in button click
        final View signInButton = findViewById(R.id.sign_in_button);
        if(signInButton != null) {
            signInButton.setOnClickListener(signInClickListener);
        }
    }
    private View.OnClickListener signInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            if(v.getId() == R.id.sign_in_button) {
                clickSignIn();
            }
        }
    };

    /**
     * Start user sign in flow
     */
    protected void clickSignIn() {
        Log.i(TAG, "User clicked sign in");
        userClickedSignIn = true;

        // Update sign in panel
        final View signInPanel = findViewById(R.id.sign_in_panel);
        if(signInPanel != null) {
            findViewById(R.id.sign_in_button).setEnabled(false);
            findViewById(R.id.sign_in_spinner).setVisibility(View.VISIBLE);
        }

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void clickSignOut() {
        Log.i(TAG, "clickSignOut");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.d(TAG, "signOut:onResult:" + status);
                        Toast.makeText(BaseActivity.this, "Signed out", Toast.LENGTH_LONG).show();

                        // Show sign in panel
                        final View signInPanel = findViewById(R.id.sign_in_panel);
                        if(signInPanel != null) {
                            signInPanel.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    protected void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        final View signInPanel = findViewById(R.id.sign_in_panel);
        if(signInPanel != null) {
            findViewById(R.id.sign_in_button).setEnabled(true);
            findViewById(R.id.sign_in_spinner).setVisibility(View.GONE);
        }
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            account = result.getSignInAccount();
            Log.i(TAG, "Signed in successfully with user " + account.getDisplayName());

            final String authCode = account.getServerAuthCode();
            Log.d(TAG, "Got auth code " + authCode); // TODO: Remove me

            final String idToken = account.getIdToken();
            Log.d(TAG, "Got id token " + idToken); // TODO: Remove me

            // Hide sign in panel
            if(signInPanel != null) {
                signInPanel.setVisibility(View.GONE);
            }
            if(userClickedSignIn) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Sign in failed");
            // Show sign in panel
            if(signInPanel != null) {
                signInPanel.setVisibility(View.VISIBLE);
            }
            if(userClickedSignIn) {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show();
            }
        }
        userClickedSignIn = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Log.i(TAG, "Not signed in");
    }

    /** Get google auth token and return asynchronously via callback */
    public void getAuthToken(final Callback<String> callback) {
        if(account != null) {
            final String token = account.getIdToken();
            if(token != null) {
                Log.i(TAG, "Got auth token " + token);
                if(callback != null) {
                    callback.apply(token);
                }
            } else {
                if(callback != null) {
                    callback.error("Failed to get auth token");
                }
            }
        } else {
            callback.error("Not signed in");
        }
    }

}
