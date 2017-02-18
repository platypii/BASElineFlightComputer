package com.platypii.baseline.cloud;

import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Callback;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.Try;
import android.os.AsyncTask;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Upload to the cloud
 */
class UploadTask extends AsyncTask<Void,Void,Try<CloudData>> {
    private static final String TAG = "UploadTask";

    private static final String postUrl = TheCloud.baselineServer + "/tracks";

    private final TrackFile trackFile;
    private final String auth;
    private final Callback<CloudData> cb;

    UploadTask(TrackFile trackFile, String auth, Callback<CloudData> cb) {
        this.trackFile = trackFile;
        this.auth = auth;
        this.cb = cb;
    }

    @Override
    protected Try<CloudData> doInBackground(Void... voids) {
        Log.i(TAG, "Uploading track with auth " + auth);
        if(trackFile.getCloudData() != null) {
            Log.e(TAG, "Track already uploaded");
        }
        try {
            // Make HTTP request
            final CloudData result = postTrack(trackFile, auth);
            // Save cloud data
            trackFile.setCloudData(result);
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
        // Notify listeners
        if(result instanceof Try.Success) {
            EventBus.getDefault().post(new SyncEvent.UploadSuccess());
            if(cb != null) {
                final CloudData cloudData = ((Try.Success<CloudData>) result).result;
                cb.apply(cloudData);
            }
        } else {
            final String error = ((Try.Failure<CloudData>) result).error;
            EventBus.getDefault().post(new SyncEvent.UploadFailure(error));
            if(cb != null) {
                cb.error(error);
            }
        }
    }

    private static CloudData postTrack(TrackFile trackFile, String auth) throws IOException, JSONException {
        final URL url = new URL(postUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/gzip");
        conn.setRequestProperty("Authorization", auth);
        final long contentLength = trackFile.file.length();
        try {
            conn.setDoOutput(true);
            // Write to OutputStream
            if(contentLength > Integer.MAX_VALUE) {
                conn.setChunkedStreamingMode(0);
            } else {
                conn.setFixedLengthStreamingMode((int) contentLength);
            }
            final InputStream is = new FileInputStream(trackFile.file);
            final OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            IOUtil.copy(is, os);
            is.close();
            os.close();
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Read body
                final String body = IOUtil.toString(conn.getInputStream());
                final JSONObject jsonObject = new JSONObject(body);
                final CloudData cloudData = CloudData.fromJson(jsonObject);
                // TODO: Move track to synced directory
//                moveTrackFile(trackFile);
                return cloudData;
            } else if(status == 401) {
                throw new IOException("authorization required");
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static void moveTrackFile(TrackFile trackFile) {
        // Ensure synced directory exists
        final File syncedDir = new File(trackFile.file.getParentFile(), "synced");
        if(!syncedDir.exists()) {
            syncedDir.mkdir();
        }
        // Move track file to synced directory
        final File destination = new File(syncedDir, trackFile.file.getName());
        trackFile.file.renameTo(destination);
    }

}
