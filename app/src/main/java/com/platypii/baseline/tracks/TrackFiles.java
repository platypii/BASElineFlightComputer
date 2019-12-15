package com.platypii.baseline.tracks;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manage track files on the device.
 * Files are saved in either the external files dir, or the internal files.
 * Unsynced track files are stored in the top level directory.
 * Synced track files are moved to the "synced" directory.
 */
class TrackFiles {
    private static final String TAG = "TrackFiles";

    @NonNull
    static List<TrackFile> getTracks(@Nullable File logDir) {
        final List<TrackFile> tracks = new ArrayList<>();
        // Load jumps from disk
        if (logDir != null) {
            final File[] files = logDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    final String filename = file.getName();
                    final TrackFile trackFile = new TrackFile(file);
                    // Tracks look like track_yyyy-MM-dd_HH-mm-ss.csv.gz
                    if (filename.endsWith(".csv.gz")) {
                        tracks.add(trackFile);
                    }
                }
            } else {
                Log.e(TAG, "Failed to list track files: " + logDir + " " + logDir.exists());
            }
            return tracks;
        } else {
            Log.e(TAG, "Track storage directory not available");
            return tracks;
        }
    }

    static File getTrackDirectory(@NonNull Context context) {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return context.getExternalFilesDir(null);
        } else {
            Log.w(TAG, "External storage directory not available, falling back to internal storage");
            return context.getFilesDir();
        }
    }

    /**
     * Generate new track file based on current timestamp
     */
    @NonNull
    static TrackFile newTrackFile(File logDir) {
        // Name file based on current timestamp
        final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        final String timestamp = dt.format(new Date());

        final File file = makeUnique(logDir, "track_" + timestamp, "csv.gz");
        return new TrackFile(file);
    }

    /**
     * When importing an existing CSV, generate filename based on source file
     */
    @NonNull
    static TrackFile newTrackFile(File logDir, @Nullable String sourceFilename) {
        if (sourceFilename == null) {
            return newTrackFile(logDir);
        } else {
            final File file = makeUnique(logDir, sourceFilename, "csv.gz");
            return new TrackFile(file);
        }
    }


    /**
     * Generate a unique file that doesn't yet exist.
     * If logdir/prefix.ext exists, generate logdir/prefix_2.ext
     */
    @NonNull
    private static File makeUnique(File logDir, String prefix, String ext) {
        File file = new File(logDir, prefix + "." + ext);

        // Avoid filename conflicts
        for (int i = 2; file.exists(); i++) {
            file = new File(logDir, prefix + "_" + i + "." + ext);
        }

        return file;
    }

}
