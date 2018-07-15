package com.platypii.baseline.views;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.util.Callback;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;

public abstract class BaseActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BaseActivity";

    /* Request codes used to invoke user interactions */
    static final int RC_SIGN_IN = 0;
    public static final int RC_LOCATION = 1;
    public static final int RC_TTS_DATA = 2;

    protected FirebaseAnalytics firebaseAnalytics;

    /* Client used to interact with Google APIs */
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount account;

    // If user didn't click, don't show sign in/out toast
    private boolean userClickedSignIn = false;

    // Sign in panel
    private View signInPanel;
    private View signInSpinner;

    // Save last sign in state so that sign in panel doesn't blink
    public static AuthEvent currentAuthState = null;
    private static final String PREF_AUTH_STATE = "auth_state";

    @Nullable
    String getDisplayName() {
        if (account != null) {
            return account.getDisplayName();
        } else {
            return null;
        }
    }

    /**
     * Update sign in state, notify listeners, and update shared UI
     */
    private void updateAuthState(@NonNull AuthEvent event) {
        currentAuthState = event;
        // Notify listeners
        EventBus.getDefault().post(event);
        // Update sign in panel state
        if (signInPanel != null) {
            if (event == AuthEvent.SIGNED_IN) {
                signInPanel.setVisibility(View.GONE);
            } else if (event == AuthEvent.SIGNING_IN) {
                signInSpinner.setVisibility(View.VISIBLE);
                signInPanel.setVisibility(View.VISIBLE);
            } else if (event == AuthEvent.SIGNED_OUT) {
                signInSpinner.setVisibility(View.GONE);
                signInPanel.setVisibility(View.VISIBLE);
            }
        }
        // Show toasts
        if (userClickedSignIn && event == AuthEvent.SIGNED_IN) {
            Toast.makeText(this, R.string.signin_success, Toast.LENGTH_LONG).show();
        } else if (userClickedSignIn && event == AuthEvent.SIGNED_OUT) {
            Toast.makeText(this, R.string.signin_failed, Toast.LENGTH_LONG).show();
        }
        // Save to preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_AUTH_STATE, event.state);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize early services
        Services.create(this);

        // Load previous auth state
        if (currentAuthState == null) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            currentAuthState = AuthEvent.fromString(prefs.getString(PREF_AUTH_STATE, null));
        }

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
        // Log.d(TAG, getClass().getSimpleName() + " starting, starting services");
        Services.start(this);

        Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient)
                .setResultCallback(this::handleSignInResult);

        // Listen for sign in button click
        signInPanel = findViewById(R.id.sign_in_panel);
        signInSpinner = findViewById(R.id.sign_in_spinner);
        final View signInButton = findViewById(R.id.sign_in_button);
        if (signInButton != null) {
            signInButton.setOnClickListener(signInClickListener);
        }
        // If we know that we are signed out, then show the panel
        if (signInPanel != null) {
            if (currentAuthState == AuthEvent.SIGNED_OUT) {
                signInPanel.setVisibility(View.VISIBLE);
            } else {
                signInPanel.setVisibility(View.GONE);
            }
        }
    }

    private final View.OnClickListener signInClickListener = v -> {
        if (v.getId() == R.id.sign_in_button) {
            clickSignIn();
        }
    };

    /**
     * Start user sign in flow
     */
    void clickSignIn() {
        Log.i(TAG, "User clicked sign in");
        firebaseAnalytics.logEvent("click_sign_in", null);
        userClickedSignIn = true;

        // Notify sign in listeners
        updateAuthState(AuthEvent.SIGNING_IN);

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    void clickSignOut() {
        Log.i(TAG, "User clicked sign out");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(status -> {
                    Log.d(TAG, "Signed out: " + status);
                    Toast.makeText(BaseActivity.this, R.string.signout_success, Toast.LENGTH_LONG).show();
                    signedOut();
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
            if (result != null) {
                handleSignInResult(result);
            }
        } else if (requestCode == RC_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Notify services that TTS is ready
                Services.onTtsLoaded(this);
            } else {
                // missing data, install it
                final Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

    }

    private void handleSignInResult(@NonNull GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult " + result.getStatus());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            account = result.getSignInAccount();
            if (account != null) {
                Log.i(TAG, "Sign in successful for user " + account.getDisplayName());

                // final String authCode = account.getServerAuthCode();
                // Log.d(TAG, "Got auth code " + authCode);

                // final String idToken = account.getIdToken();
                // Log.d(TAG, "Got id token " + idToken);

                // Update track listing
                Services.cloud.listing.listAsync(account.getIdToken(), false);
            }

            // Notify listeners
            updateAuthState(AuthEvent.SIGNED_IN);
        } else {
            if (result.getStatus().getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                // Don't sign out for network errors, base jumpers often have poor signal
                Log.w(TAG, "Sign in failed due to network error");
            } else {
                Log.w(TAG, "Sign in failed: " + result.getStatus());
                signedOut();
            }
        }
        userClickedSignIn = false;
    }

    private void signedOut() {
        // Clear account
        account = null;
        // Clear track listing
        Services.cloud.signOut();
        // Notify listeners
        updateAuthState(AuthEvent.SIGNED_OUT);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Log.i(TAG, "Not signed in - connection failed");
    }

    /**
     * Get google auth token and return asynchronously via callback
     */
    protected void getAuthToken(@NonNull Callback<String> callback) {
        if (account != null) {
            final String token = account.getIdToken();
            if (token != null) {
                Log.i(TAG, "Got auth token " + token);
                callback.apply(token);
            } else {
                callback.error("Failed to get auth token");
            }
        } else {
            callback.error("Not signed in");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_LOCATION) {
            if (grantResults.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Services.location.start(getApplication());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // If track is still recording, services will wait
        // Log.d(TAG, getClass().getSimpleName() + " stopping, stopping services");
        Services.stop();
    }

}
