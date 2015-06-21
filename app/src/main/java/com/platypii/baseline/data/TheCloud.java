package com.platypii.baseline.data;

import android.os.AsyncTask;
import android.util.Log;

import com.platypii.baseline.Try;
import com.platypii.baseline.Callback;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;

public class TheCloud {

    private static final String baselineServer = "https://base-line.ws";
    private static final String postUrl = baselineServer + "/tracks";

    public static void upload(final Jump jump, final String auth, final Callback<Try<CloudData>> cb) {
        Log.i("Cloud", "Uploading track with auth " + auth);
        new AsyncTask<Void,Void,Try<CloudData>>() {
            @Override
            protected Try<CloudData> doInBackground(Void... voids) {
                // Upload to the cloud
                return TheCloud.uploadSync(jump, auth);
            }
            @Override
            protected void onPostExecute(Try<CloudData> cloudData) {
                SyncStatus.update();
                if(cb != null) {
                    cb.apply(cloudData);
                }
            }
        }.execute();
    }

    /** Upload to the cloud */
    private static Try<CloudData> uploadSync(Jump jump, String auth) {
        // Check if track is already uploaded
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            // Already uploaded, return url
            return new Try.Success<>(cloudData);
        } else {
            // Upload to the cloud
            try {
                // Save cloud data
                final CloudData result = postJump(jump, auth);
                jump.setCloudData(result);
                Log.i("Cloud", "Upload successful, url " + result.trackUrl);
                return new Try.Success<>(result);
            } catch(IOException e) {
                Log.e("Cloud", "Failed to upload file", e);
                return new Try.Failure<>(e.getMessage());
            } catch(JSONException e) {
                Log.e("Cloud", "Failed to parse response", e);
                return new Try.Failure<>(e.toString());
            }
        }
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
            final OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            copy(new FileInputStream(file), os);
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
