package com.platypii.baseline.laser;

import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Exceptions;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.util.Collections;
import java.util.List;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_DISCONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STARTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Thread that reads from bluetooth laser rangefinder.
 * Laser measurements are emitted as EventBus messages.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class RangefinderRunnable implements Runnable {
    private static final String TAG = "RangefinderRunnable";

    @NonNull
    private final RangefinderService service;
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
    @Nullable
    private RangefinderProtocol protocol;

    RangefinderRunnable(@NonNull RangefinderService service, @NonNull Context context, @NonNull BluetoothAdapter bluetoothAdapter) {
        this.service = service;
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    /**
     * Scan for bluetooth LE devices that look like a rangefinder
     */
    @Override
    public void run() {
        Log.i(TAG, "Rangefinder bluetooth thread starting");
        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            return;
        }
        // Scan for rangefinders
        Log.i(TAG, "Scanning for rangefinder");
        service.setState(BT_STARTING);
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothScanner == null) {
            Log.e(TAG, "Failed to get bluetooth LE scanner");
            return;
        }
        final ScanFilter scanFilter = new ScanFilter.Builder().build();
        final List<ScanFilter> scanFilters = Collections.singletonList(scanFilter);
        final ScanSettings scanSettings = new ScanSettings.Builder().build();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                super.onScanResult(callbackType, result);
                if (service.getState() == BT_STARTING) {
                    final BluetoothDevice device = result.getDevice();
                    final ScanRecord record = result.getScanRecord();
                    if (ATNProtocol.isATN(device)) {
                        Log.i(TAG, "ATN rangefinder found, connecting to: " + device.getName());
                        connect(device);
                        protocol = new ATNProtocol(bluetoothGatt);
                    } else if (UineyeProtocol.isUineye(device, record)) {
                        Log.i(TAG, "Uineye rangefinder found, connecting to: " + device.getName());
                        connect(device);
                        protocol = new UineyeProtocol(bluetoothGatt);
                    } else if (SigSauerProtocol.isSigSauer(device, record)) {
                        Log.i(TAG, "SigSauer rangefinder found, connecting to: " + device.getName());
                        connect(device);
                        protocol = new SigSauerProtocol(bluetoothGatt);
                    }
                }
            }
        };
        bluetoothScanner.startScan(scanFilters, scanSettings, scanCallback);
    }

    private void connect(@NonNull BluetoothDevice device) {
        stopScan();
        service.setState(BT_CONNECTING);
        // Connect to device
        bluetoothGatt = device.connectGatt(context, true, gattCallback);
        // Log event
        final Bundle bundle = new Bundle();
        bundle.putString("device_name", device.getName());
        Analytics.logEvent(context, "rangefinder_found", bundle);
    }

    private void stopScan() {
        if (service.getState() != BT_STARTING) {
            Exceptions.report(new IllegalStateException("Scanner shouldn't exist in state " + service.getState()));
        }
        // Stop scanning
        if (bluetoothScanner != null) {
            bluetoothScanner.stopScan(scanCallback);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Rangefinder connected");
                // TODO: Do we need to discover services? Or can we just connect?
                bluetoothGatt.discoverServices();
                service.setState(BT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Rangefinder disconnected");
                service.setState(BT_DISCONNECTED);
            } else {
                Log.i(TAG, "Rangefinder state " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Rangefinder bluetooth services discovered");
                protocol.onServicesDiscovered();
            } else {
                Log.i(TAG, "Rangefinder service discovery failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
            if (ch.getUuid().equals(protocol.getCharacteristic())) {
                protocol.processBytes(ch.getValue());
            } else {
                Log.i(TAG, "Rangefinder onCharacteristicChanged " + ch);
            }
        }
    };

    void stop() {
        // Close bluetooth socket
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        // Stop scanning
        if (service.getState() == BT_STARTING) {
            stopScan();
        }
        service.setState(BT_STOPPING);
    }

}
