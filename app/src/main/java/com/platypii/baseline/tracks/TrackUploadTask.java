package com.platypii.baseline.tracks;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthException;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.AuthToken;
import com.platypii.baseline.cloud.BaselineCloud;
import com.platypii.baseline.cloud.tasks.Task;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.MD5;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.greenrobot.eventbus.EventBus;

public class TrackUploadTask extends Task {
    private static final String TAG = "UploadTask";
    private static final String postUrl = BaselineCloud.baselineServer + "/tracks";

    @NonNull
    private final TrackFile trackFile;

    TrackUploadTask(@NonNull TrackFile trackFile) {
        this.trackFile = trackFile;
    }

    @NonNull
    @Override
    public String id() {
        return trackFile.getName();
    }

    @NonNull
    @Override
    public TaskType taskType() {
        return TaskType.trackUpload;
    }

    @Override
    public void run(@NonNull Context context) throws AuthException, IOException {
        if (AuthState.getUser() == null) {
            throw new AuthException("auth required");
        }
        Log.i(TAG, "Uploading track " + trackFile);
        Services.tracks.store.setUploading(trackFile);
        try {
            // Get auth token
            final String authToken = AuthToken.getAuthToken(context);
            // Make HTTP request
            final TrackMetadata track = postTrack(trackFile, authToken);
            // Remove from track store
            Services.tracks.store.setUploadSuccess(trackFile, track);
            // Move track to synced directory
            trackFile.archive(track.localFile(context));
            // Add to cloud cache
            Services.tracks.cache.add(track);
            // Update track listing
            Services.tracks.listAsync(context, true);
            Log.i(TAG, "Upload successful, track " + track.track_id);
            EventBus.getDefault().post(new SyncEvent.UploadSuccess(trackFile, track));
        } catch (Exception e) {
            // Update track store
            Services.tracks.store.setNotUploaded(trackFile);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.UploadFailure(trackFile, e.getMessage()));
            // Re-throw exception to indicate Task failure
            throw e;
        }
    }

    /**
     * HTTP post track to baseline, parse response as TrackMetadata
     */
    @NonNull
    private static TrackMetadata postTrack(@NonNull TrackFile trackFile, String auth) throws AuthException, IOException, JsonSyntaxException {
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
                return new Gson().fromJson(body, TrackMetadata.class);
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
     * Copy bytes from track file to output stream, and update upload progress
     */
    private static void copy(@NonNull TrackFile trackFile, @NonNull OutputStream output) throws IOException {
        final InputStream is = new FileInputStream(trackFile.file);
        final byte[] buffer = new byte[1024];
        int bytesRead;
        int bytesCopied = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            bytesCopied += bytesRead;

            // Update upload progress state
            Services.tracks.store.setUploadProgress(trackFile, bytesCopied);
            EventBus.getDefault().post(new SyncEvent.UploadProgress(trackFile, bytesCopied));
        }
        is.close();
        output.flush();
    }

    @NonNull
    @Override
    public String toString() {
        return "TrackUpload(" + id() + ", " + trackFile.getName() + ")";
    }

}
