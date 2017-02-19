package com.platypii.baseline.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackFile {
    private static final String TAG = "TrackFile";

    // TrackFile info
    public final File file;

    private CloudData cloudData;

    public TrackFile(File file) {
        this.file = file;
    }

    String cacheKey() {
        return "track." + file.getName();
    }

    /**
     * Returns cloud url info, if this track has been uploaded.
     * Will not initialize an upload, and will get cached, safe to use whenever.
     */
    public CloudData getCloudData() {
        if(cloudData != null) {
            return cloudData;
        } else {
            if(Services.prefs != null) {
                // Get from KV store
                final String trackUrl = Services.prefs.getString(cacheKey(), null);
                if (trackUrl != null) {
                    final String trackKml = Services.prefs.getString(cacheKey() + ".trackKml", null);
                    cloudData = new CloudData(trackUrl, trackKml);
                    return cloudData;
                } else {
                    // Not in KV store, not uploaded
                    return null;
                }
            } else {
                // KV store not initialized?
                return null;
            }
        }
    }

    public void setCloudData(CloudData cloudData) {
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.putString(cacheKey(), cloudData.trackUrl);
        editor.putString(cacheKey() + ".trackKml", cloudData.trackKml);
        editor.commit();
        this.cloudData = cloudData;
    }

    public String getName() {
        return file.getName()
                .replaceAll(".csv.gz", "")
                .replaceAll("_", " ")
                .replaceAll("-", ".");
    }

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
        if(file.delete()) {
            cloudData = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Move the track file to synced directory
     */
    public void archive() {
        // Ensure synced directory exists
        final File syncedDir = new File(file.getParentFile(), "synced");
        if(!syncedDir.exists()) {
            syncedDir.mkdir();
        }
        // Move track file to synced directory
        final File destination = new File(syncedDir, file.getName());
        file.renameTo(destination);
    }

    @Override
    public String toString() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return format.format(getDate());
    }

}
