package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are StringBuildering correctly
 */
public class StringBuilderUtilTest {

    private final double[] testDoubles = {-1, 0, 0.1, 0.5, 0.9, 0.99, 0.995, 0.999, 1, 1.1, 2, 9.8, 10};
    private final float[] testFloats = {-1f, 0f, 0.1f, 0.5f, 0.9f, 0.99f, 0.995f, 0.999f, 1f, 1.1f, 2f, 9.8f, 10f};
    private final long[] testLongs = {0, 1, 2, 9, 10, 11, 100, 101, 1000};

    /**
     * Check that StringBuilderUtil.format2f(buf, x) == String.format("%.2f", x)
     */
    @Test
    public void format2f() {
        StringBuilder sb = new StringBuilder();

        for (float value : testFloats) {
            sb.setLength(0);
            StringBuilderUtil.format2f(sb, value);
            assertEquals(String.format("%.2f", value), sb.toString());
        }
    }

    /**
     * Check that StringBuilderUtil.format3f(buf, x) == String.format("%.3f", x)
     */
    @Test
    public void format3f() {
        StringBuilder sb = new StringBuilder();

        for (double value : testDoubles) {
            sb.setLength(0);
            StringBuilderUtil.format3f(sb, value);
            assertEquals(String.format("%.3f", value), sb.toString());
        }
    }

    /**
     * Check that StringBuilderUtil.format2c(buf, x) == String.format("%02d", x)
     */
    @Test
    public void format2d() {
        StringBuilder sb = new StringBuilder();

        for (long value : testLongs) {
            sb.setLength(0);
            StringBuilderUtil.format2d(sb, value);
            assertEquals(String.format("%02d", value), sb.toString());
        }
    }

    /**
     * Check that StringBuilderUtil.format3d(buf, x) == String.format("%03d", x)
     */
    @Test
    public void format3d() {
        StringBuilder sb = new StringBuilder();

        for (long value : testLongs) {
            sb.setLength(0);
            StringBuilderUtil.format3d(sb, value);
            assertEquals(String.format("%03d", value), sb.toString());
        }
    }

}
