package com.platypii.baseline.tracks;

import java.io.File;
import java.nio.file.Files;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalTracksTest {

    // Test track store and state machine
    @Test
    public void setUploading() throws Exception {
        LocalTracks store = new LocalTracks();

        File file = Files.createTempFile("tmp", ".csv.gz").toFile();
        TrackFile trackFile = new TrackFile(file);

        store.setRecording(trackFile);
        assertFalse(store.isUploading(trackFile));

        store.setNotUploaded(trackFile);
        assertFalse(store.isUploading(trackFile));

        store.setUploading(trackFile);
        assertTrue(store.isUploading(trackFile));
        assertEquals(0, store.getUploadProgress(trackFile));

        store.setUploadProgress(trackFile, 1000);
        assertEquals(1000, store.getUploadProgress(trackFile));

        TrackMetadata cloudData = new MockTrackMetadata();
        store.setUploadSuccess(trackFile, cloudData);
        assertFalse(store.isUploading(trackFile));
        assertEquals(cloudData, store.getCloudData(trackFile));
    }

}
