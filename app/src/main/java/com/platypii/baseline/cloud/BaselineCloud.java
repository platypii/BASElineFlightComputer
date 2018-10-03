package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.util.Network;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BaselineCloud implements BaseService {

    static final String baselineServer = "https://baseline.ws";
    static final String listUrl = baselineServer + "/v1/tracks";

    @Nullable
    private ConnectivityManager connectivityManager;
    public final TrackListing listing = new TrackListing();
    private final UploadManager uploads = new UploadManager();

    /**
     * Clear the track list cache (for when user signs out)
     */
    public void signOut() {
        listing.cache.clear();
    }

    public void deleteTrack(@NonNull CloudData track, @NonNull String auth) {
        // Delete track on server
        new Thread(new DeleteTask(auth, track)).start();
    }

    boolean isNetworkAvailable() {
        return Network.isAvailable(connectivityManager);
    }

    @Override
    public void start(@NonNull Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Start cloud services
        listing.start(context);
        uploads.start(context);
    }

    @Override
    public void stop() {
        uploads.stop();
        listing.stop();
    }

}
