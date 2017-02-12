package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Callback;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.google.firebase.crash.FirebaseCrash;
import org.json.JSONException;
import java.util.List;

public class TheCloud {
    static final String baselineServer = "https://base-line.ws";
    static final String listUrl = TheCloud.baselineServer + "/v1/tracks";

    static final String CACHE_TRACK_LIST = "cloud.track_list";

    public static TrackData getCached(String track_id) {
        final List<TrackData> tracks = listCached();
        if(tracks != null) {
            for(TrackData track : tracks) {
                if(track.track_id.equals(track_id)) {
                    return track;
                }
            }
        }
        return null;
    }

    public static List<TrackData> listCached() {
        final String jsonString = Services.prefs.getString(CACHE_TRACK_LIST, null);
        if(jsonString != null) {
            try {
                return TrackListing.fromJson(jsonString);
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Clear the track list cache (for when user signs out)
     */
    public static void clearCache() {
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.remove(CACHE_TRACK_LIST);
        editor.apply();
    }

    /**
     * Query baseline server for track listing asynchronously
     */
    public static void list(@NonNull String auth) {
        TrackListing.listTracksAsync(auth);
    }

    public static void upload(TrackFile trackFile, String auth, Callback<CloudData> cb) {
        new UploadTask(trackFile, auth, cb).execute();
    }

    public static boolean deleteTrack(TrackData track) {
        // TODO: Delete track on server
        return false;
    }
}
