package com.platypii.baseline.lasers.rangefinder;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanRecord;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;

public abstract class BleProtocol extends BluetoothPeripheralCallback {

    /**
     * Return true if the protocol knows how to use the peripheral
     */
    public abstract boolean canParse(@NonNull BluetoothPeripheral peripheral, @Nullable ScanRecord record);

    /**
     * Initialize communication with a peripheral
     */
    public abstract void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral);

    /**
     * Process messages from a peripheral
     */
    public abstract void processBytes(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value);

    @Override
    public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull final GattStatus status) {
        if (status == GattStatus.SUCCESS && value.length > 0) {
            // Send the message to the protocol for handling
            processBytes(peripheral, value);
        }
    }
}
