package com.platypii.baseline;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.data.MyLocationManager;

public class BaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BaseActivity";

    /* Client used to interact with Google APIs */
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount account;

    /* Request codes used to invoke user interactions */
    private static final int RC_SIGN_IN = 0;
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 64;
    static final int MY_TTS_DATA_CHECK_CODE = 48;

    private boolean userClickedSignIn = false;

    protected boolean isSignedIn() {
        return account != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Google sign in
        final String serverClientId = getString(R.string.server_client_id);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Start flight services
        Services.start(this);
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
    private final View.OnClickListener signInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NonNull View v){
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

                        account = null;

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
        } else if(requestCode == MY_TTS_DATA_CHECK_CODE) {
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, start the audible
                MyAudible.init(getApplicationContext());
            } else {
                // missing data, install it
                final Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

    }

    protected void handleSignInResult(GoogleSignInResult result) {
        final View signInPanel = findViewById(R.id.sign_in_panel);
        if(signInPanel != null) {
            findViewById(R.id.sign_in_button).setEnabled(true);
            findViewById(R.id.sign_in_spinner).setVisibility(View.GONE);
        }
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            account = result.getSignInAccount();
            Log.i(TAG, "Signed in successfully with user " + account.getDisplayName());

            // final String authCode = account.getServerAuthCode();
            // Log.d(TAG, "Got auth code " + authCode);

            // final String idToken = account.getIdToken();
            // Log.d(TAG, "Got id token " + idToken);

            // Hide sign in panel
            if(signInPanel != null) {
                signInPanel.setVisibility(View.GONE);
            }
            if(userClickedSignIn) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Sign in failed");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if(grantResults.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    MyLocationManager.start(getApplication());
                } catch(SecurityException e) {
                    Log.e(TAG, "Error enabling location services", e);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");

        // If track is still recording, services will take care of it
        Services.stop();
    }

}
