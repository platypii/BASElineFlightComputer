package com.platypii.baseline.tracks;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;

/**
 * Represents a track file on the local device (pre-upload)
 */
public class TrackFile {
    private static final String TAG = "TrackFile";

    // TrackFile info
    public final File file;

    public TrackFile(File file) {
        this.file = file;
    }

    @NonNull
    public String getName() {
        return file.getName()
                .replaceAll(".csv.gz", "")
                .replaceAll("_", " ");
    }

    @NonNull
    public String getSize() {
        if (!file.exists()) {
            Log.e(TAG, "Missing file in TrackFile.getSize()");
        } else if (file.length() == 0) {
            Log.e(TAG, "Zero length file in TrackFile.getSize()");
        }
        final long size = file.length() >> 10;
        return size + " kb";
    }

    @Override
    public String toString() {
        return file.getName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrackFile && file.equals(((TrackFile) obj).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

}
