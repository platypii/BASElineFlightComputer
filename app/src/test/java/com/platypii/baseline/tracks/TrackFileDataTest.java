package com.platypii.baseline.tracks;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrackFileDataTest {

    // Test parsing of FlySight date format
    // Beware java SimpleDateFormat does not handle decimal seconds well
    @Test
    public void parseFlySightDateTest() throws Exception {
        long millis = 1517000000400L;
        String str = "2018-01-26T20:53:20.40Z"; // FlySight ISO format

        long parsed = TrackFileData.parseFlySightDate(str);
        assertEquals(millis, parsed);
    }

}
