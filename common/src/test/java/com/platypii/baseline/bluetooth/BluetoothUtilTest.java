package com.platypii.baseline.bluetooth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BluetoothUtilTest {
    @Test
    public void byteArrayToHex() {
        assertEquals("66-6f-6f", BluetoothUtil.byteArrayToHex("foo".getBytes()));
    }

    @Test
    public void bytesToShort() {
        assertEquals(0x666f, BluetoothUtil.bytesToShort((byte) 0x66, (byte) 0x6f));
    }
}
