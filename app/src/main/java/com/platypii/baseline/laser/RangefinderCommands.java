package com.platypii.baseline.laser;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import static com.platypii.baseline.laser.Uineye.*;

/**
 * Commands to send to uineye rangefinder.
 */
public class RangefinderCommands {
    private static final String TAG = "RangefinderCommands";

    static void requestRangefinderService(BluetoothGatt bluetoothGatt) {
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            // Enables notification locally:
            bluetoothGatt.setCharacteristicNotification(ch, true);
            // Enables notification on the device
            final BluetoothGattDescriptor descriptor = ch.getDescriptor(clientCharacteristicDescriptor);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    static void sendHello(BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "app -> rf: hello");
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            ch.setValue(appHello);
            bluetoothGatt.writeCharacteristic(ch);
        }
    }

    static void sendHeartbeatAck(BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "app -> rf: heartbeat ack");
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            ch.setValue(appHeartbeatAck);
            bluetoothGatt.writeCharacteristic(ch);
        }
    }

}
