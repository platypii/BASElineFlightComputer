package com.platypii.baseline.cloud;

import com.platypii.baseline.places.Place;
import com.platypii.baseline.tracks.TrackFiles;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.File;

/**
 * Class representing online track info
 */
public class CloudData {
    public final String track_id;
    public final long date;
    public final String date_string;
    public final String trackUrl;
    public final String trackKml;
    @Nullable
    public final Place place;

    CloudData(String track_id, long date, String date_string, String trackUrl, String trackKml, @Nullable Place place) {
        this.track_id = track_id;
        this.date = date;
        this.date_string = date_string;
        this.trackUrl = trackUrl;
        this.trackKml = trackKml;
        this.place = place;
    }

    /**
     * Returns the file location of the local track data file
     */
    @NonNull
    File localFile(@NonNull Context context) {
        final File trackDir = TrackFiles.getTrackDirectory(context);
        return new File(trackDir, "tracks/" + track_id + "/track.csv.gz");
    }

    /**
     * Returns the file location of the local abbreviated (gps only) track data file
     */
    @NonNull
    public File abbrvFile(@NonNull Context context) {
        final File trackDir = TrackFiles.getTrackDirectory(context);
        return new File(trackDir, "tracks/" + track_id + "/track-abbrv.csv");
    }

    /**
     * Returns short "Name, Country" string, similar to old location field.
     */
    @NonNull
    public String location() {
        return place == null ? "" : place.niceString();
    }

    @Override
    public boolean equals(Object cd) {
        return cd instanceof CloudData && ((CloudData) cd).track_id.equals(track_id);
    }

}
