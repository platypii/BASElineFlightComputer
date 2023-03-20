package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

public class BluetoothUtil {

    /**
     * Convert a byte array into a human readable hex string.
     * "foo".getBytes() -> "66-6f-6f"
     */
    @NonNull
    public static String byteArrayToHex(@NonNull byte[] a) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append('-');
            }
            sb.append(String.format("%02x", a[i]));
        }
        return sb.toString();
    }

    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b1 & 0xff) << 8) | (b2 & 0xff));
    }

    @NonNull
    public static String getDeviceName(@NonNull BluetoothDevice device) {
        try {
            return device.getName();
        } catch (SecurityException ignored) {
            return "";
        }
    }

    /**
     * Sleep without exceptions
     */
    public static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException ignored) {
        }
    }
}
