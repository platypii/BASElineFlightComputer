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

    public static void format2f(StringBuffer buffer, double x) {
        final long x100 = Math.round(x * 100);
        final long whole = x100 / 100;
        final long tenths = x100 / 10 % 10;
        final long hundredths = x100 % 10;
        buffer.append(whole);
        buffer.append(".");
        buffer.append(tenths);
        buffer.append(hundredths);
    }

    public static void format3f(StringBuffer buffer, double x) {
        final long x1000 = Math.round(x * 1000);
        final long whole = x1000 / 1000;
        final long tenths = x1000 / 100 % 10;
        final long hundredths = x1000 / 10 % 10;
        final long thousandths = x1000 % 10;
        buffer.append(whole);
        buffer.append(".");
        buffer.append(tenths);
        buffer.append(hundredths);
        buffer.append(thousandths);
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
