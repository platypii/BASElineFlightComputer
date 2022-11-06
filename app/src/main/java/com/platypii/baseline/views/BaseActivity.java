package com.platypii.baseline.views;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.RequestCodes;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthException;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.BaselineAuth;
import com.platypii.baseline.util.BaseCallback;
import com.platypii.baseline.util.Exceptions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * BaseActivity provides basic services to activities that extend it.
 * Authentication mostly.
 */
public abstract class BaseActivity extends FragmentActivity {
    private final String TAG = getClass().getSimpleName();

    protected FirebaseAnalytics firebaseAnalytics;

    /* Client used to interact with Google APIs */
    @Nullable
    private GoogleSignInClient signInClient;
    @Nullable
    private GoogleSignInAccount account;

    // If user didn't click, don't show sign in/out toast
    private boolean userClickedSignIn = false;

    // Sign in panel
    private View signInPanel;
    private View signInSpinner;

    @Nullable
    String getDisplayName() {
        if (account != null) {
            return account.getDisplayName();
        } else {
            return null;
        }
    }

    /**
     * Set sign in state, notify listeners, and update shared UI.
     * Only updates on state change. Views should get auth state from AuthState directly.
     */
    private void setAuthState(@NonNull AuthState event) {
        if (!event.equals(AuthState.currentAuthState)) {
            AuthState.setState(this, event);
        }
        updateAuthState();
        // Show toasts
        if (userClickedSignIn && event instanceof AuthState.SignedIn) {
            Toast.makeText(this, R.string.signin_success, Toast.LENGTH_LONG).show();
        } else if (userClickedSignIn && event instanceof AuthState.SignedOut) {
            Toast.makeText(this, R.string.signin_failed, Toast.LENGTH_LONG).show();
        }
        // Clear laser layers
        Services.lasers.layers.layers.clear();
    }

    /**
     * Update sign in panel state in the UI.
     */
    private void updateAuthState() {
        if (signInPanel != null) {
            if (AuthState.getUser() != null) {
                signInPanel.setVisibility(View.GONE);
            } else {
                signInPanel.setVisibility(View.VISIBLE);
            }
            if (AuthState.signingIn) {
                signInSpinner.setVisibility(View.VISIBLE);
            } else {
                signInSpinner.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize early services
        Services.create(this);
    }

    @Override
    protected void onStart() {
        // Start flight services before calling super, otherwise fragments start first
        Services.start(this);

        super.onStart();

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
            if (AuthState.currentAuthState instanceof AuthState.SignedOut) {
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
     * Initialize the google sign in client
     */
    private void initSignIn() {
        if (signInClient == null) {
            // Initialize google sign in
            try {
                final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.server_client_id))
                        .requestEmail()
                        .build();
                signInClient = GoogleSignIn.getClient(this, gso);
            } catch (IllegalArgumentException e) {
                Exceptions.report(new IllegalArgumentException("Server client id = " + getString(R.string.server_client_id), e));
            }
        }
    }

    /**
     * Start user sign in flow
     */
    void clickSignIn() {
        Log.i(TAG, "User clicked sign in");
        firebaseAnalytics.logEvent("click_sign_in", null);
        initSignIn();
        if (signInClient != null) {
            userClickedSignIn = true;

            // Notify sign in listeners
            AuthState.signingIn = true;
            updateAuthState();

            final Intent signInIntent = signInClient.getSignInIntent();
            startActivityForResult(signInIntent, RequestCodes.RC_SIGN_IN);
        } else {
            Exceptions.report(new NullPointerException("Clicked sign in, but SignInClient is null"));
        }
    }

    void clickSignOut() {
        Log.i(TAG, "User clicked sign out");
        initSignIn();
        if (signInClient != null) {
            signInClient.signOut()
                    .addOnSuccessListener(aVoid -> {
                        Log.i(TAG, "Signed out");
                        Toast.makeText(BaseActivity.this, R.string.signout_success, Toast.LENGTH_LONG).show();
                        signedOut();
                    });
        } else {
            Exceptions.report(new NullPointerException("Clicked sign out, but SignInClient is null"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RequestCodes.RC_SIGN_IN) {
            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnCompleteListener(this::onSignInComplete);
        } else if (requestCode == RequestCodes.RC_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Notify services that TTS is ready
                Services.audible.onTtsLoaded(this);
            } else {
                // Missing data, install it
                Intents.installTts(this);
            }
        }

    }

    private void onSignInComplete(@NonNull Task<GoogleSignInAccount> task) {
        AuthState.signingIn = false;
        try {
            // Google signed in successfully, fetch baseline token
            account = task.getResult(ApiException.class);
            Log.i(TAG, "Sign in successful for user " + account.getDisplayName());
            final String userId = account.getId();
            final String googleToken = account.getIdToken();
            if (userId != null && googleToken != null) {
                getBaselineToken(userId, googleToken);
            } else {
                Exceptions.report(new AuthException("Failed to get google auth token"));
            }
        } catch (ApiException e) {
            onSignInFailure(e);
        }
        userClickedSignIn = false;
    }

    /**
     * Google signed in successfully, fetch baseline token
     */
    private void getBaselineToken(@NonNull String userId, @NonNull String googleToken) {
        BaselineAuth.exchangeToken(googleToken, new BaseCallback<String>() {
            @Override
            public void onSuccess(@NonNull String baselineToken) {
                firebaseAnalytics.setUserId(userId);
                setAuthState(new AuthState.SignedIn(userId, baselineToken));
            }

            @Override
            public void onFailure(@NonNull Throwable ex) {
                Log.w(TAG, "Baseline sign in failed", ex);
                signedOut();
            }
        });
    }

    private void onSignInFailure(@NonNull ApiException e) {
        if (e.getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
            // Don't sign out for network errors, base jumpers often have poor signal
            Log.w(TAG, "Sign in failed due to network error");
            if (userClickedSignIn) {
                signedOut();
            }
        } else if (e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
            Log.i(TAG, "Not signed in");
            signedOut();
        } else if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
            Log.i(TAG, "Sign in canceled");
            userClickedSignIn = false;
            signedOut();
        } else {
            final int statusCode = e.getStatusCode();
            final String error = GoogleSignInStatusCodes.getStatusCodeString(statusCode);
            Exceptions.report(new IllegalArgumentException("Sign in failed: unexpected status code " + statusCode + " " + error));
            signedOut();
        }
    }

    private void signedOut() {
        // Clear account
        account = null;
        firebaseAnalytics.setUserId(null);
        // Notify listeners
        setAuthState(new AuthState.SignedOut());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.RC_LOCATION) {
            if (grantResults.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Services.location.start(getApplication());
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // If track is still recording, services will wait
        Services.stop();
    }

}
