package com.platypii.baseline.bluetooth;

import androidx.annotation.NonNull;
import java.util.Comparator;

/**
 * Used to put GPS at top of device list
 */
public class BluetoothDeviceComparator implements Comparator<BluetoothItem> {
    @Override
    public int compare(@NonNull BluetoothItem device1, @NonNull BluetoothItem device2) {
        return score(device2) - score(device1);
    }

    private int score(@NonNull BluetoothItem device) {
        if (device.name.startsWith("Mohawk")) return 2;
        else if (device.name.startsWith("RaceBox")) return 1;
        else if (device.name.contains("GPS")) return 1;
        else return 0;
    }
}
