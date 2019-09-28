package com.platypii.baseline.laser;

class Util {

    static String byteArrayToHex(byte[] a) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append('-');
            }
            sb.append(String.format("%02x", a[i]));
        }
        return sb.toString();
    }

    static short bytesToShort(byte b1, byte b2) {
        return (short) (((b1 & 0xff) << 8) | (b2 & 0xff));
    }

    /**
     * Sleep without exceptions
     */
    static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException ignored) {}
    }
}
