package com.platypii.baseline.tracks;

import android.support.annotation.NonNull;
import java.io.File;

/**
 * Represents a track file on the local device (pre-upload)
 */
public class TrackFile {

    // TrackFile info
    public final File file;

    public TrackFile(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName()
                .replaceAll(".csv.gz", "")
                .replaceAll("_", " ")
                .replaceAll("-", ".");
    }

    @NonNull
    public String getSize() {
        final long size = file.length() / 1024;
        return size + "kb";
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
