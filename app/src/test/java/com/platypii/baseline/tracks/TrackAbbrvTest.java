package com.platypii.baseline.tracks;

import com.platypii.baseline.FileUtil;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrackAbbrvTest {

    @Test
    public void abbreviate() throws IOException {
        File in = FileUtil.makeFileGz("time,sensor,x,y,z\n,gps,1,2,3\n,alt,4,5,6\n,grv,7,8,9");
        File out = File.createTempFile("abbrv-out", ".csv");
        TrackAbbrv.abbreviate(in, out);
        assertEquals(40, out.length());
    }

}
