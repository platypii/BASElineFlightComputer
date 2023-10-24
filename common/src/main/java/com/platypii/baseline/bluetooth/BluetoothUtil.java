package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
            final String name = device.getName();
            return name == null ? "" : name;
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

    /**
     * Serialize manufacturer data into a string
     */
    public static String toManufacturerString(@Nullable ScanRecord record) {
        if (record != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final StringBuilder sb = new StringBuilder();
            final SparseArray<byte[]> mfg = record.getManufacturerSpecificData();
            for (int i = 0; i < mfg.size(); i++) {
                final String key = "" + mfg.keyAt(i);
                final String hex = byteArrayToHex(mfg.valueAt(i));
                sb.append(key);
                sb.append('=');
                sb.append(hex);
                sb.append(';');
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}
