package com.platypii.baseline;

import com.platypii.baseline.cloud.TheCloud;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.util.Callback;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;

abstract class BaseActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BaseActivity";

    FirebaseAnalytics firebaseAnalytics;

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

    protected String getDisplayName() {
        if(account != null) {
            return account.getDisplayName();
        } else {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start flight services
        Services.start(this);

        final OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        // TODO: Question, if opr.isDone, can we still just setResultCallback to have 1 code path?
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            final GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            // TODO: When Java8, use this::handleSignInResult
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
        firebaseAnalytics.logEvent("click_sign_in", null);
        userClickedSignIn = true;

        // Notify sign in listeners
        EventBus.getDefault().post(AuthEvent.SIGNING_IN);

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    protected void clickSignOut() {
        Log.i(TAG, "clickSignOut");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.d(TAG, "signOut:onResult:" + status);
                        Toast.makeText(BaseActivity.this, R.string.signout_success, Toast.LENGTH_LONG).show();

                        account = null;

                        // Notify listeners
                        EventBus.getDefault().post(AuthEvent.SIGNED_OUT);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else if(requestCode == MY_TTS_DATA_CHECK_CODE) {
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Notify services that TTS is ready
                Services.onTtsLoaded(getApplicationContext());
            } else {
                // missing data, install it
                final Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult " + result.getStatus());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            account = result.getSignInAccount();
            if(account != null) {
                Log.i(TAG, "Sign in successful for user " + account.getDisplayName());
            }

            // final String authCode = account.getServerAuthCode();
            // Log.d(TAG, "Got auth code " + authCode);

            // final String idToken = account.getIdToken();
            // Log.d(TAG, "Got id token " + idToken);

            // Update track listing
            TheCloud.listAsync(account.getIdToken(), false);

            // Notify listeners
            EventBus.getDefault().post(AuthEvent.SIGNED_IN);
            if(userClickedSignIn) {
                Toast.makeText(this, R.string.signin_success, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Sign in failed");
            // Notify listeners
            EventBus.getDefault().post(AuthEvent.SIGNED_OUT);
            if(userClickedSignIn) {
                Toast.makeText(this, R.string.signin_failed, Toast.LENGTH_LONG).show();
            }
        }
        userClickedSignIn = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Log.d(TAG, "onConnectionFailed:" + connectionResult);
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
                    Services.location.start(getApplication());
                } catch(SecurityException e) {
                    Log.e(TAG, "Error enabling location services", e);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // If track is still recording, services will wait
        Services.stop();
    }

}
