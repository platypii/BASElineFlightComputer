package com.platypii.baseline.tracks;

import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.cloud.AuthException;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.BaselineCloud;
import com.platypii.baseline.cloud.tasks.Task;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.events.SyncEvent.DownloadFailure;
import com.platypii.baseline.events.SyncEvent.DownloadProgress;
import com.platypii.baseline.events.SyncEvent.DownloadSuccess;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
public class DownloadTrackTask extends Task {
    private static final String TAG = "DownloadTask";

    @NonNull
    private final TrackMetadata track;
    @NonNull
    private final String trackUrl;

    public DownloadTrackTask(@NonNull TrackMetadata track) {
        this.track = track;
        this.trackUrl = BaselineCloud.baselineServer + "/tracks/" + track.track_id + "/baseline-track.csv.gz";
    }

    @NonNull
    @Override
    public String id() {
        return track.track_id;
    }

    @NonNull
    @Override
    public TaskType taskType() {
        return TaskType.trackUpload;
    }

    @Override
    public void run(@Nullable Context context) throws AuthException, IOException {
        if (context == null) {
            throw new NullPointerException("TrackUploadTask needs Context");
        }

        try {
            final File trackFile = track.localFile(context);
            if (!trackFile.exists()) {
                Log.i(TAG, "Downloading track " + track);
                EventBus.getDefault().post(new DownloadProgress(track, 0, 1));
                // Make HTTP request
                downloadTrack(trackFile);
                Log.i(TAG, "Download successful, track " + track);
            } else {
                Log.i(TAG, "Track file exists, skipping download " + trackFile);
            }

            final File abbrvFile = track.abbrvFile(context);
            if (!abbrvFile.exists()) {
                // Make abbrv file
                Log.i(TAG, "Generating abbreviated track file " + abbrvFile);
                TrackAbbrv.abbreviate(trackFile, abbrvFile);
            }
            // Notify listeners
            EventBus.getDefault().post(new DownloadSuccess(track, trackFile));
        } catch (Exception e) {
            Log.e(TAG, "Failed to download file", e);
            // Notify listeners
            EventBus.getDefault().post(new DownloadFailure(track, e));
            // Re-throw exception to indicate Task failure
            throw e;
        }
    }

    /**
     * HTTP get track data from baseline
     */
    private void downloadTrack(@NonNull File trackFile) throws IOException, AuthException {
        final URL url = new URL(trackUrl);
        final String auth = AuthState.getToken();
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Cookie", auth);
        conn.setRequestProperty("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME);
        conn.addRequestProperty("Accept-Encoding", "identity"); // Without this, okhttp gets confused about gzip
        try {
            // Read response
            final int status = conn.getResponseCode();
            if (status == 200) {
                // Read body
                final InputStream is = conn.getInputStream();
                copy(track, is, trackFile, conn.getContentLength());
                // TODO: Check file hash?
                Log.i(TAG, "Track download successful");
            } else if (status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("Failed to download track " + track + " http status code " + status);
            }
        } catch (IOException e) {
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
    private static void copy(@NonNull TrackMetadata track, @NonNull InputStream is, @NonNull File file, int contentLength) throws IOException {
        // Make parent directory if needed
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
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
            EventBus.getDefault().post(new DownloadProgress(track, bytesCopied, contentLength));
        }
        is.close();
        os.close();
    }

    @NonNull
    @Override
    public String toString() {
        return "TrackDownload(" + id() + ", " + track.getName() + ")";
    }

}
