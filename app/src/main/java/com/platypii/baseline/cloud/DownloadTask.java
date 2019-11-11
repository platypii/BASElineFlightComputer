package com.platypii.baseline.cloud;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.tracks.TrackAbbrv;
import com.platypii.baseline.util.Exceptions;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import org.greenrobot.eventbus.EventBus;

/**
 * Download a track file from the cloud
 */
public class DownloadTask implements Runnable {
    private static final String TAG = "DownloadTask";

    @NonNull
    private final Context context;
    @NonNull
    private final String trackId;
    @NonNull
    private final String trackUrl;
    @NonNull
    private final File trackFile;
    @NonNull
    private final File abbrvFile;

    public DownloadTask(@NonNull Context context, @NonNull CloudData track) {
        this.context = context;
        this.trackId = track.track_id;
        this.trackUrl = BaselineCloud.baselineServer + "/tracks/" + track.track_id + "/baseline-track.csv.gz";
        this.trackFile = track.localFile(context);
        this.abbrvFile = track.abbrvFile(context);
    }

    @Override
    public void run() {
        // Check for network availability. Still try to download anyway, but don't report to firebase
        final boolean networkAvailable = Services.cloud.isNetworkAvailable();
        try {
            if (!trackFile.exists()) {
                Log.i(TAG, "Downloading track " + trackId);
                // Get auth token
                final String authToken = AuthToken.getAuthToken(context);
                // Make HTTP request
                downloadTrack(authToken);
                // TODO: Check file hash?
                Log.i(TAG, "Download successful, track " + trackId);
            } else {
                Log.i(TAG, "Track file exists, skipping download " + trackFile);
            }
            if (!abbrvFile.exists()) {
                // Make abbrv file
                Log.i(TAG, "Generating abbreviated track file " + abbrvFile);
                TrackAbbrv.abbreviate(trackFile, abbrvFile);
            }
            EventBus.getDefault().post(new DownloadEvent.DownloadSuccess(trackId, trackFile));
        } catch (SocketException | UnknownHostException e) {
            Log.e(TAG, "Failed to download file", e);
            EventBus.getDefault().post(new DownloadEvent.DownloadFailure(trackId, e, networkAvailable));
        } catch (AuthException | IOException e) {
            Log.e(TAG, "Failed to download file", e);
            if (networkAvailable) {
                Exceptions.report(e);
            }
            EventBus.getDefault().post(new DownloadEvent.DownloadFailure(trackId, e, networkAvailable));
        }
    }

    /**
     * HTTP get track from baseline
     */
    private void downloadTrack(@NonNull String auth) throws IOException, AuthException {
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
                copy(trackId, is, trackFile, conn.getContentLength());
                Log.i(TAG, "Track download successful");
            } else if (status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("http status code " + status);
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception while downloading track " + trackUrl, e);
            // Remove partial file so that download will retry
            if (!trackFile.delete()) {
                Log.e(TAG, "Failed to delete file for failed track download");
            }
            // Delete parent directory if it's empty
            if (!trackFile.getParentFile().delete()) {
                Log.w(TAG, "Failed to delete track folder for failed track download");
            }
            // Rethrow exception
            throw e;
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
            if (!parent.mkdirs()) {
                Log.e(TAG, "Failed to make track directory " + parent);
            }
        }
        // Copy input stream to output stream
        final OutputStream os = new FileOutputStream(file);
        final byte[] buffer = new byte[1024];
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
