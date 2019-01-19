package com.platypii.baseline.laser;

public class Util {

    public static String byteArrayToHex(byte[] a) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append('-');
            }
            sb.append(String.format("%02x", a[i]));
        }
        return sb.toString();
    }

    public static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException ignored) {}
    }
}
