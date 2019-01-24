package com.platypii.baseline.laser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.Arrays;

import static com.platypii.baseline.laser.Uineye.*;

/**
 * Thread that reads from bluetooth rangefinder.
 * This implements protocol for reading laser measurements from a Uineye HK-1800-P.
 */
class RangefinderRunnable implements Runnable {
    private static final String TAG = "RangefinderRunnable";

    // baseline's rangefinder device id
    // TODO: Scan for rangefinder
    // TODO: Don't use hardcoded device id
    private static final String deviceId = "D4:36:39:65:76:67";

    private final RfSentenceIterator sentenceIterator = new RfSentenceIterator();

    @NonNull
    private final Context context;
    @NonNull
    private final BluetoothAdapter bluetoothAdapter;

    private BluetoothGatt bluetoothGatt;

    RangefinderRunnable(@NonNull Context context, @NonNull BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    @Override
    public void run() {
        Log.i(TAG, "Laser rangefinder bluetooth thread starting");
        if (!bluetoothAdapter.isEnabled()) {
            Log.w(TAG, "Bluetooth is not enabled");
            return;
        }
        // Get bluetooth device
        final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceId);
        // Connect to bluetooth device
        Log.i(TAG, "Rangefinder connecting to: " + bluetoothDevice.getName());
        bluetoothGatt = bluetoothDevice.connectGatt(context, true, gattCallback);
    }

    private void processSentence(byte[] value) {
        if (Arrays.equals(value, laserHello)) {
            Log.i(TAG, "rf -> app: hello");
        } else if (Arrays.equals(value, heartbeat)) {
            Log.d(TAG, "rf -> app: heartbeat");
            RangefinderCommands.sendHeartbeatAck(bluetoothGatt);
        } else if (Arrays.equals(value, norange)) {
            Log.i(TAG, "rf -> app: norange");
            RangefinderCommands.sendHeartbeatAck(bluetoothGatt);
        } else if (value[0] == 23 && value[1] == 0) {
            Uineye.processMeasurement(value);
        } else {
            Log.i(TAG, "rf -> app: data " + Util.byteArrayToHex(value));
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Rangefinder connected");
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Rangefinder disconnected");
            } else {
                Log.i(TAG, "Rangefinder " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Bluetooth services discovered");
                RangefinderCommands.sendHello(bluetoothGatt);
                Util.sleep(200); // TODO: Is this needed?
                RangefinderCommands.requestRangefinderService(bluetoothGatt);
            } else {
                Log.i(TAG, "Rangefinder service discovery failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
            if (ch.getUuid().equals(rangefinderCharacteristic)) {
                addBytesToBuffer(ch.getValue());
            } else {
                Log.i(TAG, "Rangefinder onCharacteristicChanged " + ch);
            }
        }
    };

    private void addBytesToBuffer(byte[] value) {
        sentenceIterator.addBytes(value);
        while (sentenceIterator.hasNext()) {
            processSentence(sentenceIterator.next());
        }
    }

    void stop() {
        // Close bluetooth socket
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

}
