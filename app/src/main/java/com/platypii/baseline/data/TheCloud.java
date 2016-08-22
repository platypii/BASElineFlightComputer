package com.platypii.baseline.data;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Try;
import com.platypii.baseline.util.Callback;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

public class TheCloud {
    private static final String TAG = "Cloud";

    private static final String baselineServer = "https://base-line.ws";
    private static final String postUrl = baselineServer + "/tracks";

    public static void upload(final Jump jump, final String auth, final Callback<CloudData> cb) {
        Log.i(TAG, "Uploading track with auth " + auth);
        if(jump.getCloudData() != null) {
            Log.e(TAG, "Track already uploaded");
        }
        new AsyncTask<Void,Void,Try<CloudData>>() {
            @Override
            protected Try<CloudData> doInBackground(Void... voids) {
                // Upload to the cloud
                try {
                    // Make HTTP request
                    final CloudData result = postJump(jump, auth);
                    // Save cloud data
                    jump.setCloudData(result);
                    Log.i(TAG, "Upload successful, url " + result.trackUrl);
                    return new Try.Success<>(result);
                } catch(IOException e) {
                    Log.e(TAG, "Failed to upload file", e);
                    FirebaseCrash.report(e);
                    return new Try.Failure<>(e.getMessage());
                } catch(JSONException e) {
                    Log.e(TAG, "Failed to parse response", e);
                    FirebaseCrash.report(e);
                    return new Try.Failure<>(e.toString());
                }
            }
            @Override
            protected void onPostExecute(Try<CloudData> result) {
                EventBus.getDefault().post(new SyncEvent());
                if(cb != null) {
                    if(result instanceof Try.Success) {
                        final CloudData cloudData = ((Try.Success<CloudData>) result).result;
                        cb.apply(cloudData);
                    } else {
                        final String error = ((Try.Failure<CloudData>) result).error;
                        cb.error(error);
                    }
                }
            }
        }.execute();
    }

    private static CloudData postJump(Jump jump, String auth) throws IOException, JSONException {
        final File file = jump.logFile;
        final URL url = new URL(postUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/gzip");
        conn.setRequestProperty("Authorization", auth);
        final long contentLength = file.length();
        try {
            conn.setDoOutput(true);
            // Write to OutputStream
            if(contentLength > Integer.MAX_VALUE) {
                conn.setChunkedStreamingMode(0);
            } else {
                conn.setFixedLengthStreamingMode((int) contentLength);
            }
            final InputStream is = new FileInputStream(file);
            final OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            copy(is, os);
            is.close();
            os.close();
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Read body
                final String body = toString(conn.getInputStream());
                return CloudData.fromJson(body);
            } else if(status == 401) {
                throw new IOException("authorization required");
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        final byte buffer[] = new byte[1024];
        int bytesRead;
        while((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }

    private static String toString(InputStream input) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final byte buffer[] = new byte[1024];
        int bytesRead;
        while((bytesRead = input.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, bytesRead));
        }
        return builder.toString();
    }

}
