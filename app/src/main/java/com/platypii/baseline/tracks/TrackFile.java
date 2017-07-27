package com.platypii.baseline.tracks;

import com.platypii.baseline.cloud.CloudData;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a track file on the local device (pre-upload)
 */
public class TrackFile {
    private static final String TAG = "TrackFile";

    // TrackFile info
    public File file;

    // Upload state
    public boolean uploading = false;
    public boolean uploaded = false;
    public CloudData cloudData;

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

    /**
     * Parse date from filename
     */
    private Date getDate() {
        final String dateString = getName().replaceAll("track ", "");
        final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss", Locale.US);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse date from filename", e);
            FirebaseCrash.report(e);
            return null;
        }
    }

    /** Delete local track file */
    public boolean delete() {
        Log.w(TAG, "Deleting track file " + file);
        return file.delete();
    }

    /**
     * Move the track file to synced directory
     */
    public void archive() {
        Log.i(TAG, "Archiving track file " + file.getName());
        // Ensure synced directory exists
        final File syncedDir = new File(file.getParentFile(), "synced");
        if(!syncedDir.exists()) {
            syncedDir.mkdir();
        }
        // Move track file to synced directory
        final File destination = new File(syncedDir, file.getName());
        if(file.renameTo(destination)) {
            // Move succeeded
            file = destination;
        }
    }

    @Override
    public String toString() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return format.format(getDate());
    }

}
