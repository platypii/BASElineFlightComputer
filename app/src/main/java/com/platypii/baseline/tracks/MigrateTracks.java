package com.platypii.baseline.tracks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.platypii.baseline.Services;

/**
 * Move files from the track directory into the synced directory
 */
public class MigrateTracks {
    private static final String TAG = "Migrate";

    // Has user migrated to v3?
    private static final String PREF_MIGRATE_VERSION = "baseline.migrate.version";

    public static void migrate(Context context) {
        // Check if we've already migrated
        if(Services.prefs.getInt(PREF_MIGRATE_VERSION, 2) < 3) {
            Log.w(TAG, "Migrating tracks to v3");
            final SharedPreferences.Editor editor = Services.prefs.edit();
            for(TrackFile trackFile : TrackFiles.getTracks(context)) {
                if(trackFile.isSyncedV2()) {
                    Log.w(TAG, "Moving track " + trackFile.getName());
                    trackFile.archive();
                    // Delete from preferences
                    editor.remove(trackFile.cacheKey());
                    editor.remove(trackFile.cacheKey() + ".trackKml");
                }
            }
            editor.putInt(PREF_MIGRATE_VERSION, 3);
            editor.apply();
        }
    }

}
