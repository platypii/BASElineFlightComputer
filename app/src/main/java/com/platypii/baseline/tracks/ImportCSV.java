package com.platypii.baseline.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.util.IOUtil;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
            AsyncTask.execute(() -> {
                copyFile(context, uri);
            });
            return true;
        }
        return false;
    }

    /**
     * Copy content file into a new track file
     */
    private static void copyFile(@NonNull Context context, Uri uri) {
        final ContentResolver content = context.getContentResolver();
        // Get source filename
        String sourceFilename = resolveFileName(content, uri);
        // Strip CSV extension
        if (sourceFilename != null) sourceFilename = sourceFilename.replaceAll(".(csv|CSV)$", "");
        // Generate destination file
        final File logDir = TrackFiles.getTrackDirectory(context);
        final TrackFile destination = TrackFiles.newTrackFile(logDir, sourceFilename);
        Log.i(TAG, "Importing CSV file " + uri + " as " + sourceFilename + " to " + destination);
        try (OutputStream os = new GZIPOutputStream(new FileOutputStream(destination.file))) {
            final InputStream is = content.openInputStream(uri);
            IOUtil.copy(is, os);
            Services.trackStore.setNotUploaded(destination);
        } catch (IOException e) {
            Log.e(TAG, "Failed to import CSV file", e);
        }
    }

    @Nullable
    private static String resolveFileName(ContentResolver contentResolver, Uri uri) {
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return null;
    }

}
