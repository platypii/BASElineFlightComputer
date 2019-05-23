package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.cloud.lasers.Lasers;
import com.platypii.baseline.cloud.tracks.Tracks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaselineCloud implements BaseService {

    public static final String baselineServer = "https://baseline.ws";

    @Nullable
    private ConnectivityManager connectivityManager;

    // REST objects
    public final Tracks tracks = new Tracks();
    private final UploadManager uploads = new UploadManager();
    public final Lasers lasers = new Lasers();

    public void deleteTrack(@NonNull Context context, @NonNull CloudData track) {
        new Thread(new DeleteTask(context, track)).start();
    }

    /**
     * Return true if there is a network connection available
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager != null) {
            final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } else {
            return false;
        }
    }

    @Override
    public void start(@NonNull Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Start cloud services
        tracks.start(context);
        uploads.start(context);
        lasers.start(context);
    }

    @Override
    public void stop() {
        lasers.stop();
        uploads.stop();
        tracks.stop();
    }

}
