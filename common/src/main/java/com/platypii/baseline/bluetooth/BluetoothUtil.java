package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import com.welie.blessed.BluetoothBytesParser;

public class BluetoothUtil {

    /**
     * Convert a byte array into a human readable hex string.
     * "foo".getBytes() -> "66-6f-6f"
     */
    @NonNull
    public static String byteArrayToHex(@NonNull byte[] bytes) {
        return BluetoothBytesParser.asHexString(bytes, "-");
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
