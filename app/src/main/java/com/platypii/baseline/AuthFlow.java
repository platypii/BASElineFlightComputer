package com.platypii.baseline;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

public class AuthFlow {

    private static boolean authFlowStarted = false;

    public static void startFlow(Context context) {
        if(!authFlowStarted) {
            authFlowStarted = true;
            // Ask for username
            showUsernamePrompt(context);
        } else {
            Log.e("Auth", "Auth flow started twice");
        }
    }

    private static void showUsernamePrompt(final Context context) {
        final EditText usernameView = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.username_prompt)
                .setView(usernameView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Validate password
                        final CharSequence username = usernameView.getText();
                        handleUsername(context, username);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // End application
                        Toast.makeText(context, R.string.error_username_required, Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void handleUsername(Context context, CharSequence username) {
        // Check if username exists
        try {
            if(Auth.usernameExists(username)) {
                // Ask for password
                showPasswordPrompt(context, username, false);
            } else {
                // Register new user
                showPasswordPrompt(context, username, true);
            }
        } catch (JSONException e) {
            // Failed to contact baseline server
        }
    }

    private static void showPasswordPrompt(final Context context, final CharSequence username, final boolean isSignup) {
        final EditText passwordView = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.password_prompt)
                .setView(passwordView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Validate password
                        final CharSequence password = passwordView.getText();
                        handlePassword(context, username, password, isSignup);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // End application
                        Toast.makeText(context, R.string.error_password_required, Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void handlePassword(Context context, CharSequence username, CharSequence password, boolean isSignup) {
        try {
            if(isSignup) {
                // Sign up
                if(Auth.signup(username, password)) {
                    Toast.makeText(context, "Sign up successful", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Sign up failed", Toast.LENGTH_LONG).show();
                }
            } else {
                // Sign in
                if(Auth.signin(username, password)) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Login failed", Toast.LENGTH_LONG).show();
                }
            }
        } catch(JSONException e) {}
    }

}
