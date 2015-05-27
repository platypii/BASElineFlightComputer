package com.platypii.baseline.data;

import java.io.File;

public class Jump {

    // Jump info
    public final File logFile;

    public Jump(File logFile) {
        this.logFile = logFile;
    }

    public String cacheKey() {
        return "jump-" + logFile.getName();
    }

    /** Returns cloud url if this track has been uploaded */
    public String getCloudUrl() {
        return KVStore.get(cacheKey());
    }

    @Override
    public String toString() {
        return logFile.getName();
    }
}
