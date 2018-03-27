package com.platypii.baseline.cloud;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.events.DownloadEvent;
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

/**
 * Download a track file from the cloud
 */
public class DownloadTask implements Runnable {
    private static final String TAG = "DownloadTask";

    private final Context context;
    private final CloudData track;
    private final String trackUrl;
    private final File file;

    public DownloadTask(@NonNull Context context, @NonNull CloudData track) {
        this.context = context;
        this.track = track;
        this.trackUrl = "https://baseline.ws/tracks/" + track.track_id + "/baseline-track.csv.gz";
        this.file = track.localFile(context);
    }

    @Override
    public void run() {
        Log.i(TAG, "Downloading track " + track.track_id);
        // Check if file exists
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
            Log.i(TAG, "Download successful, track " + track.track_id);
            EventBus.getDefault().post(new DownloadEvent.DownloadSuccess(track.track_id, file));
        } catch (AuthException e) {
            Log.e(TAG, "Failed to download file - auth error", e);
            if (networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new DownloadEvent.DownloadFailure(track.track_id, "auth error"));
        } catch (IOException e) {
            Log.e(TAG, "Failed to download file", e);
            if (networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new DownloadEvent.DownloadFailure(track.track_id, e.getMessage()));
        }
    }

    /**
     * HTTP get track from baseline
     */
    private void downloadTrack(@NonNull String auth) throws IOException {
        final URL url = new URL(trackUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", auth);
        conn.setRequestProperty("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME);
        conn.addRequestProperty("Accept-Encoding", "identity"); // Without this, okhttp gets confused about gzip
        try {
            // Read response
            final int status = conn.getResponseCode();
            if (status == 200) {
                // Read body
                final InputStream is = conn.getInputStream();
                copy(track.track_id, is, file, conn.getContentLength());
                Log.i(TAG, "Track download successful");
            } else if (status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("http status code " + status);
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception while downloading track " + trackUrl, e);
            // Remove partial file so that download will retry
            file.delete();
            // Delete parent directory if it's empty
            file.getParentFile().delete();
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Copy bytes from input stream to file, and update download progress
     */
    private static void copy(@NonNull String track_id, @NonNull InputStream is, @NonNull File file, int contentLength) throws IOException {
        // Make parent directory if needed
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        // Copy input stream to output stream
        final OutputStream os = new FileOutputStream(file);
        final byte buffer[] = new byte[1024];
        int bytesRead;
        int bytesCopied = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
            bytesCopied += bytesRead;

            // Update download progress state
            EventBus.getDefault().post(new DownloadEvent.DownloadProgress(track_id, bytesCopied, contentLength));
        }
        is.close();
        os.close();
    }

}
