package com.platypii.baseline;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
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
                        // End auth flow
                        authFlowStarted = false;
                        Toast.makeText(context, R.string.error_username_required, Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void handleUsername(final Context context, final CharSequence username) {
        // Check if username exists
        new AsyncTask<Void,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return Auth.usernameExists(username);
                } catch (JSONException e) {
                    // Failed to contact baseline server
                    return null;
                }
            }
            @Override
            protected void onPostExecute(Boolean usernameExists) {
                if(usernameExists == null) {
                    authFlowStarted = false;
                    // Error contacting BASEline server
                    Toast.makeText(context, "Error connecting to server", Toast.LENGTH_LONG).show();
                } else if(usernameExists) {
                    Log.i("Auth", "Username exists");
                    // Ask for password
                    showPasswordPrompt(context, username, false);
                } else {
                    Log.i("Auth", "Username does not exist");
                    // Register new user
                    showPasswordPrompt(context, username, true);
                }
            }
        }.execute();
    }

    private static void showPasswordPrompt(final Context context, final CharSequence username, final boolean isSignup) {
        final EditText passwordView = new EditText(context);
        passwordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int prompt = isSignup? R.string.signup_password_prompt : R.string.signin_password_prompt;
        new AlertDialog.Builder(context)
                .setTitle(prompt)
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
                        // End auth flow
                        authFlowStarted = false;
                        Toast.makeText(context, R.string.error_password_required, Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private static void handlePassword(final Context context, final CharSequence username, final CharSequence password, final boolean isSignup) {
        new AsyncTask<Void,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if(isSignup) {
                        return Auth.signup(username, password);
                    } else {
                        return Auth.signin(username, password);
                    }
                } catch (JSONException e) {
                    // Failed to contact baseline server
                    return null;
                }
            }
            @Override
            protected void onPostExecute(Boolean success) {
                authFlowStarted = false;
                if(success == null) {
                    // Error contacting BASEline server
                    Toast.makeText(context, "Error connecting to server", Toast.LENGTH_LONG).show();
                } else if(isSignup) {
                    if(success) {
                        Auth.setAuth(context, username.toString());
                        Toast.makeText(context, "Sign up successful", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Sign up failed", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(success) {
                        Auth.setAuth(context, username.toString());
                        Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Login failed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }.execute();
    }

}
