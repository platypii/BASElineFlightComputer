package com.platypii.baseline.cloud;

import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Callback;
import java.util.List;

public class TheCloud {
    static final String baselineServer = "https://base-line.ws";

    public static void list(String auth, Callback<List<TrackData>> cb) {
        new ListTask(auth, cb).execute();
    }

    public static void upload(TrackFile trackFile, String auth, Callback<CloudData> cb) {
        new UploadTask(trackFile, auth, cb).execute();
    }

}
