package com.platypii.baseline.cloud;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.MD5;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

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
        final boolean networkAvailable = Services.cloud.isNetworkAvailable();
        try {
            // Get auth token
            final String authToken = AuthToken.getAuthToken(context);
            // Make HTTP request
            final CloudData track = postTrack(trackFile, authToken);
            // Remove from track store
            Services.trackStore.setUploadSuccess(trackFile, track);
            // Move track to synced directory
            archive(track);
            // Add to cloud cache
            Services.cloud.listing.cache.addTrack(track);
            // Update track listing
            Services.cloud.listing.listAsync(authToken, true);
            Log.i(TAG, "Upload successful, track " + track.track_id);
            EventBus.getDefault().post(new SyncEvent.UploadSuccess(trackFile, track));
        } catch (AuthException e) {
            // getAuthToken fails if network is unavailable
            if (networkAvailable) {
                Log.e(TAG, "Failed to upload file: auth error", e);
                Exceptions.report(e);
            } else {
                Log.w(TAG, "Failed to upload file: auth error", e);
            }
            uploadFailed(new SyncEvent.UploadFailure(trackFile, "auth error"));
        } catch (SocketException | SSLException | UnknownHostException e) {
            Log.e(TAG, "Failed to upload file, network exception, network = " + networkAvailable, e);
            uploadFailed(new SyncEvent.UploadFailure(trackFile, e.getMessage()));
        } catch (IOException e) {
            if (networkAvailable) {
                Log.e(TAG, "Failed to upload file", e);
                Exceptions.report(e);
            } else {
                Log.w(TAG, "Failed to upload file, network not available", e);
            }
            uploadFailed(new SyncEvent.UploadFailure(trackFile, e.getMessage()));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse response", e);
            Exceptions.report(e);
            uploadFailed(new SyncEvent.UploadFailure(trackFile, "invalid response from server"));
        }
    }

    private void uploadFailed(SyncEvent.UploadFailure event) {
        // Update track store
        Services.trackStore.setNotUploaded(event.trackFile);
        // Notify listeners
        EventBus.getDefault().post(event);
    }

    /**
     * HTTP post track to baseline, parse response as CloudData
     */
    @NonNull
    private CloudData postTrack(@NonNull TrackFile trackFile, String auth) throws IOException, JSONException {
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
            if (contentLength > Integer.MAX_VALUE) {
                conn.setChunkedStreamingMode(0);
            } else {
                conn.setFixedLengthStreamingMode((int) contentLength);
            }
            final OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            copy(trackFile, os);
            os.close();
            // Read response
            final int status = conn.getResponseCode();
            if (status == 200) {
                // Read body
                final String body = IOUtil.toString(conn.getInputStream());
                final JSONObject jsonObject = new JSONObject(body);
                return CloudData.fromJson(jsonObject);
            } else if (status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Move the track file to track directory
     */
    private void archive(CloudData trackData) {
        Log.i(TAG, "Archiving track file " + trackFile.getName() + " to " + trackData.track_id);
        // Move form source to destination
        final File source = trackFile.file;
        final File destination = trackData.localFile(context);
        // Ensure track directory exists
        final File trackDir = destination.getParentFile();
        if (!trackDir.exists()) {
            if (!trackDir.mkdirs()) {
                Log.e(TAG, "Failed to make track directory " + trackDir);
            }
        }
        // Move track file to track directory
        if (!source.renameTo(destination)) {
            Log.e(TAG, "Failed to move track file " + source + " to " + destination);
        }
    }

    /**
     * Copy bytes from track file to output stream, and update upload progress
     */
    private static void copy(@NonNull TrackFile trackFile, @NonNull OutputStream output) throws IOException {
        final InputStream is = new FileInputStream(trackFile.file);
        final byte buffer[] = new byte[1024];
        int bytesRead;
        int bytesCopied = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            bytesCopied += bytesRead;

            // Update upload progress state
            Services.trackStore.setUploadProgress(trackFile, bytesCopied);
            EventBus.getDefault().post(new SyncEvent.UploadProgress(trackFile, bytesCopied));
        }
        is.close();
        output.flush();
    }

}
