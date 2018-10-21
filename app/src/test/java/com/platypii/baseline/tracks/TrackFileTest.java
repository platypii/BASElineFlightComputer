package com.platypii.baseline.tracks;

import java.io.File;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrackFileTest {

    @Test
    public void trackFileName() {
        TrackFile trackFile = new TrackFile(new File("track_2018-01-01_16-20-00.csv.gz"));
        assertEquals("track 2018-01-01 16-20-00", trackFile.getName());
    }

    @Test
    public void trackFileSize() {
        TrackFile trackFile = new TrackFile(new File("track_2018-01-01_16-20-00.csv.gz"));
        assertEquals("0 kb", trackFile.getSize());
    }

}
