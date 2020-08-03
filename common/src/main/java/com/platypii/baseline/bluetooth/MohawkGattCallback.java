package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import androidx.annotation.NonNull;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;

/**
 * GATT callback for LE GPS device
 */
public class MohawkGattCallback extends BluetoothGattCallback {
    private static final String TAG = "MohawkGattCallback";

    @NonNull
    private final MohawkProtocol proto;
    @NonNull
    private final MohawkRunnable mohawkRunnable;

    MohawkGattCallback(@NonNull MohawkRunnable mohawkRunnable) {
        this.mohawkRunnable = mohawkRunnable;
        this.proto = new MohawkProtocol(mohawkRunnable.service.locationUpdates);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Mohawk connected");
                gatt.discoverServices();
                mohawkRunnable.setState(BT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Mohawk disconnected");
                gatt.close();
                mohawkRunnable.onDisconnect();
            }
        } else {
            gatt.close();
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Mohawk remote disconnect");
                mohawkRunnable.onDisconnect();
            } else {
                Log.e(TAG, "Mohawk connection state error " + status + " " + newState);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            proto.onServicesDiscovered(gatt);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
        proto.processBytes(ch.getValue());
    }
}
