package com.platypii.baseline.cloud;

import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Callback;

public class BaselineCloud {

    static final String baselineServer = "https://base-line.ws";
    static final String listUrl = BaselineCloud.baselineServer + "/v1/tracks";

    public final TrackListing listing = new TrackListing();
    public final TrackDatabase tracks = new TrackDatabase();

    /**
     * Clear the track list cache (for when user signs out)
     */
    public void signOut() {
        listing.cache.clear();
    }

    public void upload(TrackFile trackFile, String auth, Callback<CloudData> cb) {
        new UploadTask(trackFile, auth, cb).execute();
    }

    public void deleteTrack(CloudData track, String auth) {
        // Delete track on server
        TrackDelete.deleteAsync(auth, track);
    }
}
