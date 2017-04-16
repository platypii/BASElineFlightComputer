package com.platypii.baseline.cloud;

import com.platypii.baseline.Service;
import android.content.Context;

public class BaselineCloud implements Service {

    static final String baselineServer = "https://base-line.ws";
    static final String listUrl = BaselineCloud.baselineServer + "/v1/tracks";

    public final TrackListing listing = new TrackListing();
    public final TrackDatabase tracks = new TrackDatabase();
    public final UploadManager uploads = new UploadManager();

    /**
     * Clear the track list cache (for when user signs out)
     */
    public void signOut() {
        listing.cache.clear();
    }

    public void deleteTrack(CloudData track, String auth) {
        // Delete track on server
        TrackDelete.deleteAsync(auth, track);
    }

    @Override
    public void start(Context context) {
        // Start the upload manager
        uploads.start(context);
    }

    @Override
    public void stop() {
        uploads.stop();
    }

}
