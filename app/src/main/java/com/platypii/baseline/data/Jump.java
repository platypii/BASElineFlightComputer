package com.platypii.baseline.data;

import java.io.File;

public class Jump {

    // Jump info
    public final File logFile;

    public Jump(File logFile) {
        this.logFile = logFile;
    }

    private String cacheKey() {
        return "jump-" + logFile.getName();
    }

    /** Returns cloud url if this track has been uploaded */
    public String getCloudUrl() {
        return KVStore.getString(cacheKey());
    }

    public void setCloudUrl(String url) {
        KVStore.put(cacheKey(), url);
    }

    @Override
    public String toString() {
        return logFile.getName();
    }
}
