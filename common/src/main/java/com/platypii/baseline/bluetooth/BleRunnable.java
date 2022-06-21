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

        central.stopScan();
        // Close bluetooth socket
//        if (bluetoothSocket != null) {
//            try {
//                bluetoothSocket.close();
//            } catch (IOException e) {
//                Log.w(TAG, "Exception closing bluetooth socket", e);
//            }
//        }
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
                Log.i(TAG, "scan result " + peripheral + " " + scanResult);
                central.stopScan();
                central.autoConnectPeripheral(peripheral, service.getListener());
            }
            public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
                Log.i(TAG, "peripheral connected");
                peripheral.setNotify(BleHelper.SERVICE_UUID, BleHelper.CHAR_TX, true);
            }

            public void onConnectionFailed(BluetoothPeripheral peripheral, HciStatus status) {
                Log.i(TAG, "peripheral connection failed");
            }
        };

        central = new BluetoothCentralManager(activity, bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
        central.scanForPeripheralsWithServices(new UUID[]{BleHelper.SERVICE_UUID});
        return true;
    }
}
