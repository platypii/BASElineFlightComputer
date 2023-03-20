package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import java.util.Comparator;

import static com.platypii.baseline.bluetooth.BluetoothUtil.getDeviceName;

/**
 * Used to put GPS at top of device list
 */
public class BluetoothDeviceComparator implements Comparator<BluetoothDevice> {
    @Override
    public int compare(@NonNull BluetoothDevice device1, @NonNull BluetoothDevice device2) {
        return score(device2) - score(device1);
    }

    private int score(@NonNull BluetoothDevice device) {
        return getDeviceName(device).contains("GPS") ? 1 : 0;
    }
}
