package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import java.util.Comparator;

/**
 * Used to put GPS at top of device list
 */
public class BluetoothDeviceComparator implements Comparator<BluetoothDevice> {
    @Override
    public int compare(@NonNull BluetoothDevice device1, @NonNull BluetoothDevice device2) {
        return score(device2) - score(device1);
    }

    private int score(@NonNull BluetoothDevice device) {
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.contains("GPS")) {
            return 1;
        } else {
            return 0;
        }
    }
}
