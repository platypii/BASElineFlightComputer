package com.platypii.baseline.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JumpLog {

    public static synchronized List<Jump> getJumps(Context appContext) {
        final List<Jump> jumps = new ArrayList<>();
        // Load jumps from disk
        final File logDir = appContext.getExternalFilesDir(null);
        if(logDir != null) {
            final File[] files = logDir.listFiles();
            for (File file : files) {
                jumps.add(new Jump(file));
            }
            return jumps;
        } else {
            Log.e("JumpLog", "External storage directory not available");
            return jumps;
        }
    }

    public static File getLogDirectory(Context context) {
        return context.getExternalFilesDir(null);
    }

}
