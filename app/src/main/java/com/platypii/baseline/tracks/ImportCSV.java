package com.platypii.baseline.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.util.IOUtil;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.*;
import java.util.zip.GZIPOutputStream;

public class ImportCSV {
    private static final String TAG = "ImportCSV";

    /**
     * Check if activity was opened with a CSV file, and import if so
     * @return true if a track was imported
     */
    public static boolean importIntent(@NonNull Context context, Intent intent) {
        final String intentType = intent.getType();
        if (intentType != null && (intentType.contains("text/comma-separated-values") || intentType.contains("text/csv"))) {
            final Uri uri = intent.getData();
            Log.i(TAG, "Importing CSV file " + uri);
            copyFile(context, uri);
            return true;
        }
        return false;
    }

    /**
     * Copy content file into a new track file
     */
    private static void copyFile(@NonNull Context context, Uri uri) {
        final TrackFile destination = TrackFiles.newTrackFile(Services.trackStore.logDir);
        final ContentResolver content = context.getContentResolver();
        try (OutputStream os = new GZIPOutputStream(new FileOutputStream(destination.file))) {
            final InputStream is = content.openInputStream(uri);
            IOUtil.copy(is, os);
            Services.trackStore.setNotUploaded(destination);
        } catch (IOException e) {
            Log.e(TAG, "Failed to import CSV file", e);
        }
    }

}
