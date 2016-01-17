package com.platypii.baseline;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "SignIn";

    private View signinButton;
    private ProgressBar spinner;
    private TextView statusText;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

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

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // TODO: .requestServerAuthCode(serverClientId, false)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onStart() {
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
            // showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    // hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, start main app.
            final GoogleSignInAccount acct = result.getSignInAccount();
            if(acct != null) {
                Log.i(TAG, "Signed in successfully with user " + acct.getDisplayName());

                final String authCode = acct.getServerAuthCode();
                Log.d(TAG, "Got auth code " + authCode); // TODO: Remove me

                // Start main activity
                final Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "Account should not be null");
            }
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
