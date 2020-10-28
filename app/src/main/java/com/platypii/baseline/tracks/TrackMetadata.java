package com.platypii.baseline.tracks;

import com.platypii.baseline.places.Place;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class representing online track info
 */
public class TrackMetadata {
    public final String track_id;
    public final long date;
    public final String date_string;
    public final String trackUrl;
    public final String trackKml;
    @Nullable
    public final String jumpType;
    @Nullable
    public final Place place;
    @Nullable
    public String suit;
    @Nullable
    public String canopy;

    TrackMetadata(String track_id, long date, String date_string, String trackUrl, String trackKml, @Nullable Place place, @Nullable String jumpType) {
        this.track_id = track_id;
        this.date = date;
        this.date_string = date_string;
        this.trackUrl = trackUrl;
        this.trackKml = trackKml;
        this.place = place;
        this.jumpType = jumpType;
    }

    /**
     * Returns the file location of the local track data file
     */
    @NonNull
    public File localFile(@NonNull Context context) {
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

    /**
     * Returns "Place - Suit"
     */
    @NonNull
    public String subtitle() {
        if (place != null && suit != null) {
            return place.niceString() + " (" + suit + ")";
        } else if (place != null) {
            return place.niceString();
        } else if (suit != null) {
            return "(" + suit + ")";
        } else {
            return "";
        }
    }

    @NonNull
    public String getName() {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final String shortDate = df.format(new Date(date));
        final String shortLocation = place == null ? "" : place.niceString();
        return shortDate + " " + shortLocation;
    }

    public boolean isBASE() {
        if (jumpType != null) {
            return "BASE".equals(jumpType);
        } else {
            return place != null && place.isBASE();
        }
    }

    public boolean isSkydive() {
        if (jumpType != null) {
            return "Skydive".equals(jumpType);
        } else {
            return place != null && "DZ".equals(place.objectType);
        }
    }

    @Override
    public boolean equals(Object cd) {
        return cd instanceof TrackMetadata && ((TrackMetadata) cd).track_id.equals(track_id);
    }

    @NonNull
    @Override
    public String toString() {
        return track_id;
    }

}
