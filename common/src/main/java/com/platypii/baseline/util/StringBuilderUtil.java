package com.platypii.baseline.util;

import androidx.annotation.NonNull;

public class StringBuilderUtil {

    /**
     * Acts like StringBuilder.append(String.format("%.2f", x)) but avoids allocating any memory.
     */
    public static void format2f(@NonNull StringBuilder sb, float x) {
        final int x100 = Math.round(x * 100);
        final int whole = x100 / 100;
        final int tenths = x100 / 10 % 10;
        final int hundredths = x100 % 10;
        sb.append(whole);
        sb.append(".");
        sb.append(tenths);
        sb.append(hundredths);
    }

    public static void format3f(@NonNull StringBuilder sb, double x) {
        final long x1000 = Math.round(x * 1000);
        final long whole = x1000 / 1000;
        final long tenths = x1000 / 100 % 10;
        final long hundredths = x1000 / 10 % 10;
        final long thousandths = x1000 % 10;
        sb.append(whole);
        sb.append(".");
        sb.append(tenths);
        sb.append(hundredths);
        sb.append(thousandths);
    }

    /**
     * Acts like StringBuilder.append(String.format("%02d", x)) but avoids allocating any memory.
     */
    public static void format2d(@NonNull StringBuilder sb, long x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }

    /**
     * Acts like StringBuilder.append(String.format("%03d", x)) but avoids allocating any memory.
     */
    public static void format3d(@NonNull StringBuilder sb, long x) {
        if (x < 10) {
            sb.append("00");
        } else if (x < 100) {
            sb.append('0');
        }
        sb.append(x);
    }

}
