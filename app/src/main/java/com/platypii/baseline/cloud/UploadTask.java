package com.platypii.baseline.cloud;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.MD5;
import com.platypii.baseline.util.Network;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
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
        // Check for network availability. Still try to upload anyway, but don't report to firebase
        final boolean networkAvailable = Network.isAvailable(context);
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
            if(networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new SyncEvent.UploadFailure(trackFile, "auth error"));
        } catch(IOException e) {
            Log.e(TAG, "Failed to upload file", e);
            if(networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new SyncEvent.UploadFailure(trackFile, e.getMessage()));
        } catch(JSONException e) {
            Log.e(TAG, "Failed to parse response", e);
            if(networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new SyncEvent.UploadFailure(trackFile, "invalid response from server"));
        }
    }

    /**
     * HTTP post track to baseline, parse response as CloudData
     */
    @NonNull
    private static CloudData postTrack(@NonNull TrackFile trackFile, String auth) throws IOException, JSONException {
        final long contentLength = trackFile.file.length();
        final String md5 = MD5.md5(trackFile.file);
        final URL url = new URL(postUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/gzip");
        conn.setRequestProperty("Authorization", auth);
        conn.setRequestProperty("ETag", md5);
        conn.setRequestProperty("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME);
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
