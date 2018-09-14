package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseService;
import android.content.Context;
import android.support.annotation.NonNull;

public class BaselineCloud implements BaseService {

    static final String baselineServer = "https://baseline.ws";
    static final String listUrl = baselineServer + "/v1/tracks";

    public final TrackListing listing = new TrackListing();
    private final UploadManager uploads = new UploadManager();

    /**
     * Clear the track list cache (for when user signs out)
     */
    public void signOut() {
        listing.cache.clear();
    }

    public void deleteTrack(CloudData track, @NonNull String auth) {
        // Delete track on server
        new Thread(new DeleteTask(auth, track)).start();
    }

    @Override
    public void start(@NonNull Context context) {
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
