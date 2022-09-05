package com.platypii.baseline.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.location.GpsStatus;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import androidx.annotation.NonNull;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

public class BleRunnable extends AbstractBluetoothRunnable {
    static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    static final UUID CHAR_RX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); // to gps
    static final UUID CHAR_TX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"); // from gps

    private static final String TAG = "BleRunnable";
    private final BleService service;
    private final Activity activity;
    private BluetoothCentralManager central;

    public BleRunnable(@NonNull BleService service, Activity activity) {
        this.service = service;
        this.activity = activity;
    }

    @Override
    public void stop() {
        service.setState(BT_STOPPING);

        // TODO: How do we disconnect/unsubscribe from the BLE device?
        central.stopScan();
    }

    @Override
    protected AbstractBluetoothService getService() {
        return service;
    }

    @Override
    protected boolean connect() {
        BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
            @Override
            public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
                // TODO: use preferences for the exact device ide to connect to
                Log.i(TAG, "scan result " + peripheral + " " + scanResult);
                central.stopScan();
                central.autoConnectPeripheral(peripheral, service.getListener());
            }

            public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
                Log.i(TAG, "peripheral connected");
                peripheral.setNotify(SERVICE_UUID, CHAR_TX, true);
            }

            public void onConnectionFailed(BluetoothPeripheral peripheral, HciStatus status) {
                Log.i(TAG, "peripheral connection failed");
            }
        };

        central = new BluetoothCentralManager(activity, bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
        central.scanForPeripheralsWithServices(new UUID[]{SERVICE_UUID});
        return true;
    }
}
