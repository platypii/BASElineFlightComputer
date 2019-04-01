package com.platypii.baseline.views;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.util.Exceptions;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
     * Update sign in state, notify listeners, and update shared UI.
     * Only updates on state change. Views should get auth state from AuthState directly.
     */
    private void updateAuthState(@NonNull AuthState event) {
        if (!event.equals(AuthState.currentAuthState)) {
            AuthState.setState(this, event);
            // Notify listeners
            EventBus.getDefault().post(event);
            // Update sign in panel state
            if (signInPanel != null) {
                if (event instanceof AuthState.SignedIn) {
                    signInPanel.setVisibility(View.GONE);
                } else if (event instanceof AuthState.SigningIn) {
                    signInSpinner.setVisibility(View.VISIBLE);
                    signInPanel.setVisibility(View.VISIBLE);
                } else if (event instanceof AuthState.SignedOut) {
                    signInSpinner.setVisibility(View.GONE);
                    signInPanel.setVisibility(View.VISIBLE);
                }
            }
            // Show toasts
            if (userClickedSignIn && event instanceof AuthState.SignedIn) {
                Toast.makeText(this, R.string.signin_success, Toast.LENGTH_LONG).show();
            } else if (userClickedSignIn && event instanceof AuthState.SignedOut) {
                Toast.makeText(this, R.string.signin_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize early services
        Services.create(this);

        // Initialize Google sign in
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

    @Override
    protected void onStart() {
        super.onStart();

        // Start flight services
        Services.start(this);

        if (signInClient != null) {
            signInClient.silentSignIn().addOnCompleteListener(this::onSignInComplete);
        }

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
     * Start user sign in flow
     */
    void clickSignIn() {
        Log.i(TAG, "User clicked sign in");
        firebaseAnalytics.logEvent("click_sign_in", null);
        if (signInClient != null) {
            userClickedSignIn = true;

            // Notify sign in listeners
            updateAuthState(new AuthState.SigningIn());

            final Intent signInIntent = signInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            Exceptions.report(new NullPointerException("Clicked sign in, but SignInClient is null"));
        }
    }

    void clickSignOut() {
        Log.i(TAG, "User clicked sign out");
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
            final String userId = account.getId();
            firebaseAnalytics.setUserId(userId);
            updateAuthState(new AuthState.SignedIn(userId));

            // Update track listing
            Services.cloud.listing.listAsync(account.getIdToken(), false);
        } else {
            Exceptions.report(new NullPointerException("Sign in success, but account is null"));
        }

        // Notify listeners
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
        // Notify listeners
        updateAuthState(new AuthState.SignedOut());
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
