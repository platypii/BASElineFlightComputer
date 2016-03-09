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
                    return new Try.Failure<>(e.getMessage());
                } catch(JSONException e) {
                    Log.e(TAG, "Failed to parse response", e);
                    return new Try.Failure<>(e.toString());
                }
            }
            @Override
            protected void onPostExecute(Try<CloudData> result) {
                SyncStatus.update();
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

    /**
     * Delete a track from the server. Return success via callback.
     */
    public static void delete(final Jump jump, final String auth, final Callback<Void> cb) {
        Log.i(TAG, "Deleting track with auth " + auth);
        if(jump.getCloudData() != null) {
            new AsyncTask<Void,Void,Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    // Delete from the cloud
                    return deleteJump(jump, auth);
                }
                @Override
                protected void onPostExecute(Boolean success) {
                    SyncStatus.update();
                    if(cb != null) {
                        if(success) {
                            cb.apply(null);
                        } else {
                            cb.error("Failed to delete track");
                        }
                    }
                }
            }.execute();
        } else {
            Log.e(TAG, "Cannot delete track from server, not uploaded");
        }
    }

    private static boolean deleteJump(Jump jump, String auth) {
        try {
            final URL url = new URL(jump.getCloudData().trackUrl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", auth);
            try {
                // Read response
                final int status = conn.getResponseCode();
                if(status == 200) {
                    Log.i(TAG, "Track deleted from server");
                    return true;
                } else {
                    Log.e(TAG, "Failed to delete track, http status code " + status);
                    return false;
                }
            } finally {
                conn.disconnect();
            }
        } catch(Exception e) {
            Log.e(TAG, "Failed to delete track", e);
            return false;
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
