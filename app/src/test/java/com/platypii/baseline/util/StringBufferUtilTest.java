package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are StringBuffering correctly
 */
public class StringBufferUtilTest {

    private final float[] testFloats = {-1f, 0f, 0.1f, 0.5f, 0.9f, 0.99f, 0.995f, 0.999f, 1f, 1.1f, 2f, 9.8f, 10f};
    private final long[] testLongs = {0, 1, 2, 9, 10, 11, 100, 101, 1000};

    /**
     * Check that StringBufferUtil.format2f(buf, x) == String.format("%.2f", x)
     */
    @Test
    public void format2f() {
        StringBuffer buffer = new StringBuffer();

        for (float value : testFloats) {
            buffer.setLength(0);
            StringBufferUtil.format2f(buffer, value);
            assertEquals(String.format("%.2f", value), buffer.toString());
        }
    }

    /**
     * Check that StringBufferUtil.format2c(buf, x) == String.format("%02d", x)
     */
    @Test
    public void format2d() {
        StringBuffer buffer = new StringBuffer();

        for (long value : testLongs) {
            buffer.setLength(0);
            StringBufferUtil.format2d(buffer, value);
            assertEquals(String.format("%02d", value), buffer.toString());
        }
    }

    /**
     * Check that StringBufferUtil.format3d(buf, x) == String.format("%03d", x)
     */
    @Test
    public void format3d() {
        StringBuffer buffer = new StringBuffer();

        for (long value : testLongs) {
            buffer.setLength(0);
            StringBufferUtil.format3d(buffer, value);
            assertEquals(String.format("%03d", value), buffer.toString());
        }
    }

}
