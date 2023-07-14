package com.platypii.baseline.bluetooth;

import com.platypii.baseline.util.Exceptions;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.platypii.baseline.bluetooth.BluetoothUtil.getDeviceName;

public class BluetoothItem {
    public final String name;
    public final String address;
    public boolean ble;

    public BluetoothItem(@NonNull BluetoothDevice device) {
        name = getDeviceName(device);
        address = device.getAddress();
        ble = false;
        try {
            ble = device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC;
        } catch (SecurityException e) {
            Exceptions.report(e);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof BluetoothItem) && ((BluetoothItem) obj).address.equals(address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
