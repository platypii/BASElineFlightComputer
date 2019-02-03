package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseService;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaselineCloud implements BaseService {

    static final String baselineServer = "https://baseline.ws";
    static final String listUrl = baselineServer + "/v1/tracks";

    @Nullable
    private ConnectivityManager connectivityManager;

    // REST objects
    public final TrackListing listing = new TrackListing();
    private final UploadManager uploads = new UploadManager();
    public final LaserListing lasers = new LaserListing();

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
        listing.start(context);
        uploads.start(context);
        lasers.start(context);
    }

    @Override
    public void stop() {
        lasers.stop();
        uploads.stop();
        listing.stop();
    }

}
