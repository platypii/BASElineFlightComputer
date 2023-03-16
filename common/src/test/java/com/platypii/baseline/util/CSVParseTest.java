package com.platypii.baseline.util;

import org.junit.Test;

import static com.platypii.baseline.util.CSVParse.parseFlySightDate;
import static org.junit.Assert.assertEquals;

public class CSVParseTest {

    // Test parsing of FlySight date format
    // Beware java SimpleDateFormat does not handle decimal seconds well
    @Test
    public void parseFlySightDateTest() throws Exception {
        final long millis = 1517000000400L;
        final long seconds = 1517000000000L;

        // FlySight ISO format
        assertEquals(millis, parseFlySightDate("2018-01-26T20:53:20.40Z"));
        // FlySight2 ISO format
        assertEquals(millis, parseFlySightDate("2018-01-26T20:53:20.400Z"));

        // Tenths
        assertEquals(millis, parseFlySightDate("2018-01-26T20:53:20.4Z"));
        // Micro
        assertEquals(millis, parseFlySightDate("2018-01-26T20:53:20.400000Z"));
        assertEquals(millis, parseFlySightDate("2018-01-26T20:53:20.400999Z"));
        // No millis
        assertEquals(seconds, parseFlySightDate("2018-01-26T20:53:20Z"));
    }

}
