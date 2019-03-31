package com.platypii.baseline.cloud;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.tasks.AuthRequiredException;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.MD5;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedOutputStream;
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

/**
 * Upload to the cloud
 */
public class UploadTask {
    private static final String TAG = "UploadTask";

    private static final String postUrl = BaselineCloud.baselineServer + "/tracks";

    public static void upload(@NonNull Context context, @NonNull TrackFile trackFile) throws AuthRequiredException, UploadFailedException {
        if (AuthState.getUser() == null) {
            throw new AuthRequiredException();
        }
        Log.i(TAG, "Uploading track " + trackFile);
        Services.trackStore.setUploading(trackFile);
        try {
            // Get auth token
            final String authToken = AuthToken.getAuthToken(context);
            // Make HTTP request
            final CloudData track = postTrack(trackFile, authToken);
            // Remove from track store
            Services.trackStore.setUploadSuccess(trackFile, track);
            // Move track to synced directory
            trackFile.archive(track.localFile(context));
            // Add to cloud cache
            Services.cloud.listing.cache.add(track);
            // Update track listing
            Services.cloud.listing.listAsync(authToken, true);
            Log.i(TAG, "Upload successful, track " + track.track_id);
            EventBus.getDefault().post(new SyncEvent.UploadSuccess(trackFile, track));
        } catch (SocketException | SSLException | UnknownHostException e) {
            Log.w(TAG, "Failed to upload file, network exception", e);
            uploadFailed(new SyncEvent.UploadFailure(trackFile, e.getMessage()));
        } catch (IOException e) {
            uploadFailed(new SyncEvent.UploadFailure(trackFile, e.getMessage()));
        } catch (JsonSyntaxException e) {
            Exceptions.report(e);
            uploadFailed(new SyncEvent.UploadFailure(trackFile, "invalid response from server"));
        }
    }

    /**
     * Update track store, notify listeners, and then throw exception to indicate Task failure.
     */
    private static void uploadFailed(@NonNull SyncEvent.UploadFailure event) throws UploadFailedException {
        // Update track store
        Services.trackStore.setNotUploaded(event.trackFile);
        // Notify listeners
        EventBus.getDefault().post(event);
        throw new UploadFailedException(event);
    }

    /**
     * HTTP post track to baseline, parse response as CloudData
     */
    @NonNull
    private static CloudData postTrack(@NonNull TrackFile trackFile, String auth) throws IOException, JsonSyntaxException {
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
                return new Gson().fromJson(body, CloudData.class);
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
            Services.trackStore.setUploadProgress(trackFile, bytesCopied);
            EventBus.getDefault().post(new SyncEvent.UploadProgress(trackFile, bytesCopied));
        }
        is.close();
        output.flush();
    }

}
