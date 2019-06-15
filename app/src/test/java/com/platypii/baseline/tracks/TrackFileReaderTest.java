package com.platypii.baseline.tracks;

import com.platypii.baseline.FileUtil;
import com.platypii.baseline.measurements.MLocation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrackFileReaderTest {

    @Test
    public void read() throws IOException {
        assertEquals(0, parse("").size());
        assertEquals(0, parse("millis,sensor,lat,lon,alt\n").size());
        assertEquals(1, parse("millis,sensor,lat,lon,alt\n1,gps,2,3,4").size());
        assertEquals(1, parse("millis,sensor,lat,lon,alt\n1,gps,2,3,4\n").size());
        assertEquals(1, parse("millis,sensor,lat,lon,alt\n1,gps,2,3,4\n5,alt,6,7,8").size());
        assertEquals(1, parse("millis,sensor,lat,lon,alt\n1,gps,2,3,4\n5,alt,6,7,8\n9,alt,10,11,12").size());
        assertEquals(2, parse("millis,sensor,lat,lon,alt\n1,gps,2,3,4\n5,gps,6,7,8").size());

        // FlySight style
        assertEquals(1, parse("time,lat,lon,alt\n2019-04-20T16:20:00.00Z,2,3,4").size());
    }

    @Test
    public void corrupted() throws IOException {
        assertEquals(1, parse("millis,sensor,lat,lon,alt\n1,gps,2,3,4\n5,").size());
        assertEquals(1, parse("millis,sensor,lat,lon,alt\n1,gps,2,3,NOPE").size());
        assertEquals(0, parse("millis,sensor,lat,lon,alt\n1,gps,2,NOPE").size());
        assertEquals(0, parse("time,lat,lon,alt\nNOPE,2,3,4").size());
    }

    @Test
    public void readTwice() throws IOException {
        final TrackFileReader reader = new TrackFileReader(FileUtil.makeFile("millis,sensor,lat,lon,alt\n1,gps,2,3,4"));
        assertEquals(1, reader.read().size());
        assertEquals(1, reader.read().size());
    }

    /**
     * Write string to temp file, and parse with TrackFileReader
     */
    private List<MLocation> parse(String content) throws IOException {
        return new TrackFileReader(FileUtil.makeFile(content)).read();
    }

}
