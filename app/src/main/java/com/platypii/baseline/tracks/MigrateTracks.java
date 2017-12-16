package com.platypii.baseline.tracks;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Move files from the track directory into the synced directory
 */
public class MigrateTracks {
    private static final String TAG = "Migrate";

    // Has user migrated to v3?
    private static final String PREF_MIGRATE_VERSION = "baseline.migrate.version";

    public static void migrate(@NonNull Context context) {
        // Check if we've already migrated
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getInt(PREF_MIGRATE_VERSION, 2) < 3) {
            Log.w(TAG, "Migrating tracks to v3");
            final SharedPreferences.Editor editor = prefs.edit();
            for(TrackFile trackFile : TrackFiles.getTracks(context)) {
                if(prefs.getString(cacheKey(trackFile), null) != null) {
                    Log.w(TAG, "Archiving " + trackFile.file.getName());
                    trackFile.archive();
                    // Delete from preferences
                    editor.remove(cacheKey(trackFile));
                    editor.remove(cacheKey(trackFile) + ".trackKml");
                }
            }
            editor.putInt(PREF_MIGRATE_VERSION, 3);
            editor.apply();
        }
    }

    @NonNull
    private static String cacheKey(@NonNull TrackFile trackFile) {
        return "track." + trackFile.file.getName();
    }

}
