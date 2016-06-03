package com.platypii.baseline.data;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JumpLog {
    private static final String TAG = "JumpLog";

    public static synchronized List<Jump> getJumps(@NonNull Context appContext) {
        final List<Jump> jumps = new ArrayList<>();
        // Load jumps from disk
        final File logDir = getLogDirectory(appContext);
        if(logDir != null) {
            final File[] files = logDir.listFiles();
            for (File file : files) {
                jumps.add(new Jump(file));
            }
            // Sort by date descending
            Collections.sort(jumps, new Comparator<Jump>() {
                @Override
                public int compare(Jump track1, Jump track2) {
                    return -track1.getDate().compareTo(track2.getDate());
                }
            });
            return jumps;
        } else {
            Log.e(TAG, "Track storage directory not available");
            return jumps;
        }
    }

    public static File getLogDirectory(@NonNull Context context) {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return context.getExternalFilesDir(null);
        } else {
            Log.w(TAG, "External storage directory not available, falling back to internal storage");
            return context.getFilesDir();
        }
    }

}
