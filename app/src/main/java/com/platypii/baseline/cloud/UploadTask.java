package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.MD5;
import android.content.Context;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Upload to the cloud
 */
class UploadTask implements Runnable {
    private static final String TAG = "UploadTask";

    private static final String postUrl = BaselineCloud.baselineServer + "/tracks";

    private final Context context;
    private final TrackFile trackFile;

    UploadTask(Context context, TrackFile trackFile) {
        this.context = context;
        this.trackFile = trackFile;
    }

    @Override
    public void run() {
        Log.i(TAG, "Uploading track " + trackFile);
        try {
            // Get auth token
            final String authToken = AuthToken.getAuthToken(context);
            // Make HTTP request
            final CloudData trackData = postTrack(trackFile, authToken);
            // Move track to synced directory
            trackFile.archive();
            // Add to cache
            Services.cloud.tracks.addTrackData(trackData);
            // Update track listing
            Services.cloud.listing.listAsync(authToken, true);
            Log.i(TAG, "Upload successful, track " + trackData.track_id);
            EventBus.getDefault().post(new SyncEvent.UploadSuccess(trackFile, trackData));
        } catch(AuthException e) {
            Log.e(TAG, "Failed to upload file - auth error", e);
            FirebaseCrash.report(e);
            EventBus.getDefault().post(new SyncEvent.UploadFailure(trackFile, "auth error"));
        } catch(IOException e) {
            Log.e(TAG, "Failed to upload file", e);
            FirebaseCrash.report(e);
            EventBus.getDefault().post(new SyncEvent.UploadFailure(trackFile, e.getMessage()));
        } catch(JSONException e) {
            Log.e(TAG, "Failed to parse response", e);
            FirebaseCrash.report(e);
            EventBus.getDefault().post(new SyncEvent.UploadFailure(trackFile, "invalid response from server"));
        }
    }

    /**
     * HTTP post track to baseline, parse response as CloudData
     */
    private static CloudData postTrack(TrackFile trackFile, String auth) throws IOException, JSONException {
        final long contentLength = trackFile.file.length();
        final String md5 = MD5.md5(trackFile.file);
        final URL url = new URL(postUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/gzip");
        conn.setRequestProperty("Authorization", auth);
        conn.setRequestProperty("ETag", md5);
        // Log.d(TAG, "Uploading file with size " + contentLength);
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
                return CloudData.fromJson(jsonObject);
            } else if(status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

}
