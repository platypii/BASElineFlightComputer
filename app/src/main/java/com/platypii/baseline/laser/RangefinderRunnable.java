package com.platypii.baseline.laser;

import com.platypii.baseline.util.Exceptions;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.*;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.platypii.baseline.laser.Uineye.*;

/**
 * Thread that reads from bluetooth rangefinder.
 * This implements protocol for reading laser measurements from a Uineye HK-1800-P.
 */
class RangefinderRunnable implements Runnable {
    private static final String TAG = "RangefinderRunnable";

    private final RfSentenceIterator sentenceIterator = new RfSentenceIterator();

    @NonNull
    private final Context context;
    @NonNull
    private final BluetoothAdapter bluetoothAdapter;
    @Nullable
    private BluetoothGatt bluetoothGatt;
    @Nullable
    private BluetoothLeScanner bluetoothScanner;
    @Nullable
    private ScanCallback scanCallback;

    // State machine
    private static final int BT_STOPPED = 0;
    private static final int BT_SCANNING = 1;
    private static final int BT_CONNECTING = 2;
    private static final int BT_CONNECTED = 3;
    private static final int BT_DISCONNECTED = 4;
    private static final int BT_STOPPING = 5;
    private int state = BT_STOPPED;

    RangefinderRunnable(@NonNull Context context, @NonNull BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    @Override
    public void run() {
        Log.i(TAG, "Rangefinder bluetooth thread starting");
        if (!bluetoothAdapter.isEnabled()) {
            Log.w(TAG, "Bluetooth is not enabled");
            return;
        }
        // Scan for rangefinders
        // TODO: Set timeout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startScan();
        } else {
            Log.e(TAG, "Android 5.0+ required for bluetooth LE");
        }
    }

    /**
     * Scan for bluetooth LE devices that look like a rangefinder
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startScan() {
        Log.i(TAG, "Scanning for rangefinder");
        state = BT_SCANNING;
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothScanner == null) {
            Log.e(TAG, "Failed to get bluetooth LE scanner");
            return;
        }
        final ScanFilter scanFilter = new ScanFilter.Builder()
                .setManufacturerData(manufacturerId, manufacturerData)
                .setServiceUuid(new ParcelUuid(rangefinderService))
                .build();
        final List<ScanFilter> scanFilters = Collections.singletonList(scanFilter);
        final ScanSettings scanSettings = new ScanSettings.Builder().build();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (state == BT_SCANNING) {
                    // Stop scanning and get device
                    stopScan();
                    final BluetoothDevice device = result.getDevice();
                    Log.i(TAG, "Rangefinder found, connecting to: " + device.getName());
                    state = BT_CONNECTING;
                    bluetoothGatt = device.connectGatt(context, true, gattCallback);
                }
            }
        };
        bluetoothScanner.startScan(scanFilters, scanSettings, scanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopScan() {
        if (bluetoothScanner != null) {
            if (state != BT_SCANNING) {
                Exceptions.report(new IllegalStateException("Scanner shouldn't exist except in state BT_SCANNING"));
            }
            bluetoothScanner.stopScan(scanCallback);
        }
    }

    private void processSentence(byte[] value) {
        if (Arrays.equals(value, laserHello)) {
            Log.i(TAG, "rf -> app: hello");
            state = BT_CONNECTED;
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
                // TODO: Do we need to discover services? Or can we just connect?
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Rangefinder disconnected");
                state = BT_DISCONNECTED;
            } else {
                Log.i(TAG, "Rangefinder state " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Bluetooth services discovered for device, saying hello");
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
        // Stop scanning
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopScan();
        }
        state = BT_STOPPING;
    }

}
