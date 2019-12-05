package com.platypii.baseline.lasers.rangefinder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LaserUtilTest {
    @Test
    public void byteArrayToHex() {
        assertEquals("66-6f-6f", Util.byteArrayToHex("foo".getBytes()));
    }

    @Test
    public void bytesToShort() {
        assertEquals(0x666f, Util.bytesToShort((byte) 0x66, (byte) 0x6f));
    }
}
