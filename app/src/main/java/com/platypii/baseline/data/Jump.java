package com.platypii.baseline.data;

import java.io.File;

public class Jump {

    // Jump info
    public final File logFile;

    private CloudData cloudData;

    public Jump(File logFile) {
        this.logFile = logFile;
    }

    private String cacheKey() {
        return "jump-" + logFile.getName();
    }

    /** Returns cloud url info, if this track has been uploaded */
    public CloudData getCloudData() {
        if(cloudData != null) {
            return cloudData;
        } else {
            // Get from KV store
            final String trackUrl = KVStore.getString(cacheKey());
            if(trackUrl != null) {
                final String trackKml = KVStore.getString(cacheKey() + ".trackKml");
                cloudData = new CloudData(trackUrl, trackKml);
                return cloudData;
            } else {
                // Not in KV store, not uploaded
                return null;
            }
        }
    }

    public void setCloudData(CloudData cloudData) {
        KVStore.put(cacheKey(), cloudData.trackUrl);
        KVStore.put(cacheKey() + ".trackKml", cloudData.trackKml);
        this.cloudData = cloudData;
    }

    public String getName() {
        return logFile.getName()
                .replaceAll(".csv.gz", "")
                .replaceAll("_", " ")
                .replaceAll("-", ".");
    }

    @Override
    public String toString() {
        final long size = logFile.length() / 1024;
        final String synced = getCloudData() != null? "✓" : "↻";
        return getName() + " (" + size + "kb) " + synced;
    }

}
