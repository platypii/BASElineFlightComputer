package com.platypii.baseline;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "SignIn";

    private View signinButton;
    private ProgressBar spinner;
    private TextView statusText;

    /* true only for silent sign in */
    private boolean firstSignin = true;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Find views
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        statusText = (TextView) findViewById(R.id.status_text);
        signinButton = findViewById(R.id.sign_in_button);
        spinner = (ProgressBar) findViewById(R.id.spinner);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            // Sign in in progress
            signinButton.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            statusText.setText("");
            firstSignin = false;

            final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    protected void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, start main activity
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            if(!firstSignin) {
                // Could not resolve the connection result, show the user an error dialog.
                signinButton.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                statusText.setText(R.string.signin_failed);
                statusText.setTextColor(0xffdd1111);
                Log.e(TAG, "Sign in failed");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);

        // Could not resolve the connection result, show the user an error dialog.
        statusText.setText(R.string.signin_failed);
        statusText.setTextColor(0xffdd1111);
        Log.e(TAG, "Sign in failed: " + connectionResult);
    }
}
