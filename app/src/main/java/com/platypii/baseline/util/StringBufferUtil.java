package com.platypii.baseline.util;

public class StringBufferUtil {

    /**
     * Acts like StringBuffer.append(String.format("%.2f", x)) but avoids allocating any memory.
     */
    public static void format2f(StringBuffer buffer, float x) {
        final int x100 = Math.round(x * 100);
        final int whole = x100 / 100;
        final int tenths = x100 / 10 % 10;
        final int hundredths = x100 % 10;
        buffer.append(whole);
        buffer.append(".");
        buffer.append(tenths);
        buffer.append(hundredths);
    }

    /**
     * Acts like StringBuffer.append(String.format("%02d", x)) but avoids allocating any memory.
     */
    public static void format2d(StringBuffer buffer, long x) {
        if (x < 10) {
            buffer.append('0');
        }
        buffer.append(x);
    }

    /**
     * Acts like StringBuffer.append(String.format("%03d", x)) but avoids allocating any memory.
     */
    public static void format3d(StringBuffer buffer, long x) {
        if (x < 10) {
            buffer.append("00");
        } else if (x < 100) {
            buffer.append('0');
        }
        buffer.append(x);
    }

}
