package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class BleHelper extends BleManager {

    static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    static final UUID CHAR_RX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); // to gps
    static final UUID CHAR_TX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"); // from gps

    private BluetoothGattCharacteristic txCharacteristic;
    private BluetoothGattCharacteristic rxCharacteristic;
    private boolean supported;

    public BleHelper(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new RaceBoxBleGattCallback();
    }

    private class RaceBoxBleGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            // This means e.g. enabling notifications, setting notification callbacks,
            // sometimes writing something to some Control Point.
            requestMtu(512).enqueue();
            setNotificationCallback(txCharacteristic)
                    .with((device, data) -> Log.i("evan", data.toString()));
            enableNotifications(txCharacteristic).enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service != null) {
                txCharacteristic = service.getCharacteristic(CHAR_TX);
                rxCharacteristic = service.getCharacteristic(CHAR_RX);
            }

            boolean canListen = false;
            if (txCharacteristic != null) {
                final int txProperties = txCharacteristic.getProperties();
                canListen = (txProperties & BluetoothGattCharacteristic.PROPERTY_READ) > 0;
            }

            return supported = txCharacteristic != null && canListen;
        }

        @Override
        protected void onServicesInvalidated() {
            txCharacteristic = null;
            rxCharacteristic = null;
        }
    }
}
