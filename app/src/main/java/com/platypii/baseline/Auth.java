package com.platypii.baseline;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class Auth {

    private static final String AUTH_KEY = "is_authenticated";

    /** Return an ID string unique to this device */
    public static String getId() {
        // return Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    public static String getAuth(Context context) {
        // Check if user has authenticated
        final SharedPreferences prefs = context.getSharedPreferences("baseline", Context.MODE_PRIVATE);
        return prefs.getString(AUTH_KEY, null);
    }
    public static void setAuthenticated(Context context) {
        // Update preferences
        final SharedPreferences.Editor editor = context.getSharedPreferences("baseline", Context.MODE_PRIVATE).edit();
        editor.putBoolean(Auth.AUTH_KEY, true);
        editor.apply();
    }

    public static boolean usernameExists(CharSequence username) throws JSONException {
        final String postUrl = "https://base-line.ws/users/" + username;
        try {
            Log.i("Auth", "Validating username " + username + " at url " + postUrl);
            final int status = postJson(postUrl, null);
            return (status == 200);
        } catch(IOException e) {
            Log.e("Auth", "Username validation failed", e);
            return false;
        }
    }

    public static boolean signup(CharSequence username, CharSequence password) throws JSONException {
        final String postUrl = "https://base-line.ws/auth/signup";
        // Construct JSON request
        final JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", username);
        try {
            Log.i("Auth", "Register user " + username + " at url " + postUrl);
            final int status = postJson(postUrl, json);
            if (status == 200) {
                return true;
            } else {
                Log.e("Auth", "Userpass registration failed " + status);
                return false;
            }
        } catch(IOException e) {
            Log.e("Auth", "Userpass registration failed", e);
            return false;
        }
    }

    /** Validate against baseline server */
    public static boolean signin(CharSequence username, CharSequence password) throws JSONException {
        final String postUrl = "https://base-line.ws/auth/signin";
        // Construct JSON request
        final JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", username);
        try {
            Log.i("Auth", "Validating user " + username + " at url " + postUrl);
            final int status = postJson(postUrl, json);
            if (status == 200) {
                return true;
            } else {
                Log.e("Auth", "Userpass validation failed " + status);
                return false;
            }
        } catch(IOException e) {
            Log.e("Auth", "Userpass validation failed", e);
            return false;
        }
    }

    /** POST json to a url, return status code */
    private static int postJson(String postUrl, JSONObject json) throws IOException {
        final URL url = new URL(postUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            if(json != null) {
                // Write to request body
                conn.setDoOutput(true);
                final byte[] content = json.toString().getBytes();
                conn.setFixedLengthStreamingMode(content.length);
                final OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(content);
                os.close();
            }
            // Read response
            final int status = conn.getResponseCode();
            return status;
        } finally {
            conn.disconnect();
        }
    }

}
