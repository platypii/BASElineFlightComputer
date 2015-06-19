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

    private static final String AUTH_KEY = "auth_token";

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
    public static void setAuth(Context context, String auth) {
        // Update preferences
        final SharedPreferences.Editor editor = context.getSharedPreferences("baseline", Context.MODE_PRIVATE).edit();
        editor.putString(Auth.AUTH_KEY, auth);
        editor.apply();
    }

    /** Validate against baseline server */
    public static boolean signin(CharSequence token) throws JSONException {
        final String postUrl = "https://base-line.ws/auth/gtoken";
        // Construct JSON request
        final JSONObject json = new JSONObject();
        json.put("id_token", token);
        try {
            Log.i("Auth", "Validating user - url = " + postUrl + ", token = " + token);
            final int status = postJson(postUrl, json);
            if (status == 200) {
                Log.i("Auth", "Sign in success");
                return true;
            } else {
                Log.w("Auth", "Sign in failed " + status);
                return false;
            }
        } catch(IOException e) {
            Log.e("Auth", "Token validation failed", e);
            return false;
        }
    }

    /** POST json to a url, return status code */
    private static int postJson(String postUrl, JSONObject json) throws IOException {
        final URL url = new URL(postUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
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
