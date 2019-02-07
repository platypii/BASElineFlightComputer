package com.platypii.baseline.views;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.util.Exceptions;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;

public abstract class BaseActivity extends FragmentActivity {
    private final String TAG = getClass().getSimpleName();

    /* Request codes used to invoke user interactions */
    private static final int RC_SIGN_IN = 0;
    public static final int RC_LOCATION = 1;
    public static final int RC_TTS_DATA = 2;

    protected FirebaseAnalytics firebaseAnalytics;

    /* Client used to interact with Google APIs */
    private GoogleSignInClient signInClient;
    @Nullable
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
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start flight services
        Services.start(this);

        signInClient.silentSignIn().addOnCompleteListener(this::onSignInComplete);

        // Bind sign in panel
        signInPanel = findViewById(R.id.sign_in_panel);
        if (signInPanel != null) {
            signInSpinner = findViewById(R.id.sign_in_spinner);
            // Listen for sign in button click
            final View signInButton = findViewById(R.id.sign_in_button);
            if (signInButton != null) {
                signInButton.setOnClickListener(signInClickListener);
            }
            // If we know that we are signed out, then show the panel
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

        final Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    void clickSignOut() {
        Log.i(TAG, "User clicked sign out");
        signInClient.signOut()
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Signed out");
                    Toast.makeText(BaseActivity.this, R.string.signout_success, Toast.LENGTH_LONG).show();
                    signedOut();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnCompleteListener(this::onSignInComplete);
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

    private void onSignInComplete(Task<GoogleSignInAccount> task) {
        try {
            account = task.getResult(ApiException.class);
            onSignInSuccess();
        } catch (ApiException e) {
            onSignInFailure(e);
        }
        userClickedSignIn = false;
    }
    private void onSignInSuccess() {
        // Signed in successfully, show authenticated UI.
        if (account != null) {
            Log.i(TAG, "Sign in successful for user " + account.getDisplayName());
            firebaseAnalytics.setUserId(account.getId());

            // Update track listing
            Services.cloud.listing.listAsync(account.getIdToken(), false);
        } else {
            Exceptions.report(new NullPointerException("Sign in success, but null account"));
        }

        // Notify listeners
        updateAuthState(AuthEvent.SIGNED_IN);
    }
    private void onSignInFailure(@NonNull ApiException e) {
        if (e.getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
            // Don't sign out for network errors, base jumpers often have poor signal
            Log.w(TAG, "Sign in failed due to network error");
            if (userClickedSignIn) {
                signedOut();
            }
        } else if (e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
            Log.i(TAG, "Sign in required (user is not signed in)");
            signedOut();
        } else {
            Log.w(TAG, "Sign in failed: " + CommonStatusCodes.getStatusCodeString(e.getStatusCode()));
            signedOut();
        }
    }

    private void signedOut() {
        // Clear account
        account = null;
        firebaseAnalytics.setUserId(null);
        // Clear track listing
        Services.cloud.signOut();
        // Notify listeners
        updateAuthState(AuthEvent.SIGNED_OUT);
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
        Services.stop();
    }

}
