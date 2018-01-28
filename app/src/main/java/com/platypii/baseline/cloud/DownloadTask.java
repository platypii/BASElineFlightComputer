package com.platypii.baseline.cloud;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.tracks.TrackFiles;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Network;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

/**
 * Download a track file from the cloud
 */
class DownloadTask implements Runnable {
    private static final String TAG = "DownloadTask";

    private final Context context;
    private final String track_id;
    private final String trackUrl;
    private final File file;

    DownloadTask(@NonNull Context context, @NonNull String track_id) {
        this.context = context;
        this.track_id = track_id;
        this.trackUrl = "https://baseline.ws/tracks/" + track_id + "/track.csv";
        final File trackDir = TrackFiles.getTrackDirectory(context);
        this.file = new File(trackDir, "tracks/" + track_id);
    }

    @Override
    public void run() {
        Log.i(TAG, "Downloading track " + track_id);
        // TODO: Check if file exists
        if (file.exists()) {
            Log.e(TAG, "Overwriting existing track file " + file);
        }
        // Check for network availability. Still try to download anyway, but don't report to firebase
        final boolean networkAvailable = Network.isAvailable(context);
        try {
            // Get auth token
            final String authToken = AuthToken.getAuthToken(context);
            // Make HTTP request
            downloadTrack(authToken);
            // TODO: Check file hash?
            Log.i(TAG, "Download successful, track " + track_id);
            EventBus.getDefault().post(new DownloadEvent.DownloadSuccess(track_id));
        } catch(AuthException e) {
            Log.e(TAG, "Failed to download file - auth error", e);
            if(networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new DownloadEvent.DownloadFailure(track_id, "auth error"));
        } catch(IOException e) {
            Log.e(TAG, "Failed to download file", e);
            if(networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new DownloadEvent.DownloadFailure(track_id, e.getMessage()));
        } catch(JSONException e) {
            Log.e(TAG, "Failed to parse response", e);
            if(networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new DownloadEvent.DownloadFailure(track_id, "invalid response from server"));
        }
    }

    /**
     * HTTP get track from baseline
     */
    private void downloadTrack(@NonNull String auth) throws IOException, JSONException {
        final URL url = new URL(trackUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", auth);
        conn.setRequestProperty("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME);
        // Log.d(TAG, "Uploading file with size " + contentLength);
        try {
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Read body
                final InputStream is = conn.getInputStream();
                copy(track_id, is, file);
                Log.i(TAG, "Track download successful");
            } else if(status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Copy bytes from input stream to file, and update download progress
     */
    private static void copy(@NonNull String track_id, @NonNull InputStream is, @NonNull File file) throws IOException {
        final OutputStream os = new FileOutputStream(file);
        final byte buffer[] = new byte[1024];
        int bytesRead;
        int bytesCopied = 0;
        while((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
            bytesCopied += bytesRead;

            // Update download progress state
            EventBus.getDefault().post(new DownloadEvent.DownloadProgress(track_id, bytesCopied));
        }
        is.close();
        os.close();
    }

}
