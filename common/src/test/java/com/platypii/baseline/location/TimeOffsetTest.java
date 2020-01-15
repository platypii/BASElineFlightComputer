package com.platypii.baseline.location;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are syncing time correctly.
 * TODO: This test occasionally fails due to race condition on calling System.currentTimeMillis.
 */
public class TimeOffsetTest {

    @Test
    public void timeOffset() {
        assertEquals(123, TimeOffset.gpsToPhoneTime(123));
        assertEquals(123, TimeOffset.phoneToGpsTime(123));
        // Initial update
        TimeOffset.update("test", System.currentTimeMillis() + 10000L);
        assertEquals(10123, TimeOffset.phoneToGpsTime(123));
        // Phone is behind warn
        TimeOffset.update("test", System.currentTimeMillis() + 5000L);
        assertEquals(10123, TimeOffset.phoneToGpsTime(123));
        // Phone is behind adjust
        TimeOffset.update("test", System.currentTimeMillis() + 15000L);
        assertEquals(15123, TimeOffset.phoneToGpsTime(123));
        // Phone is ahead
        TimeOffset.update("test", System.currentTimeMillis() - 100000L);
        assertEquals(-99877, TimeOffset.phoneToGpsTime(123));
    }

}
