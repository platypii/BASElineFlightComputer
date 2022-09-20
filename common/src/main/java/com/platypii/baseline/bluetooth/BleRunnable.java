package com.platypii.baseline.bluetooth;

import android.app.Activity;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.HciStatus;

import java.util.Optional;
import java.util.UUID;

import androidx.annotation.NonNull;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_STARTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

public class BleRunnable extends AbstractBluetoothRunnable {
    static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    static final UUID CHAR_RX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); // to gps
    static final UUID CHAR_TX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"); // from gps

    private static final String TAG = "BleRunnable";
    private final BleService service;
    private BluetoothCentralManager central;
    private Optional<BluetoothPeripheral> connectedPeripheral;

    public BleRunnable(@NonNull BleService service, Activity activity) {
        this.service = service;
        connectedPeripheral = Optional.empty();

        BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
            @Override
            public void onDiscoveredPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull ScanResult scanResult) {
                // TODO: use preferences for the exact device id to connect to
                Log.d(TAG, "BLE scan result " + peripheral + " " + scanResult);
                service.notifyScanListeners(scanResult);
            }

            public void onConnectedPeripheral(@NonNull BluetoothPeripheral peripheral) {
                Log.i(TAG, "BLE peripheral connected: " + peripheral.getAddress() + " \"" + peripheral.getName() + "\"");

                connectedPeripheral = Optional.of(peripheral);
                peripheral.setNotify(SERVICE_UUID, CHAR_TX, true);
            }

            public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral, @NonNull HciStatus status) {
                Log.e(TAG, "BLE peripheral connection failed: " + status);
            }
        };

        central = new BluetoothCentralManager(activity, bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void stop() {
        service.setState(BT_STOPPING);

        central.stopScan();
        disconnect();
    }

    @Override
    protected AbstractBluetoothService getService() {
        return service;
    }

    public void startScan() {
        // is disconnect needed? or does it just not report connected devices? does the device not advertise when it is connected?
        // this seems to have issues if the scan is started between app start and device connection

        disconnect();
        central.scanForPeripheralsWithServices(new UUID[]{SERVICE_UUID});
    }

    public void stopScan() {
        service.setState(BT_STARTING);
        central.stopScan();
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public void disconnect() {
        connectedPeripheral.ifPresent(p -> p.setNotify(SERVICE_UUID, CHAR_TX, false)); // is both this and cancelConnection necessary?
        connectedPeripheral.ifPresent(central::cancelConnection);
        connectedPeripheral = Optional.empty();
    }

    @Override
    protected boolean connect() {
        // Since this happens on startup it will keep the device active while the app is in the foreground
        // We should have some auto sleep for the device once it is 'warmed up'
        // Alternatively, it could be a manual action to initiate the warmup
        if (service.preferences.preferenceDeviceId != null) {
            Log.i(TAG, "Reconnecting to saved BLE device: " + service.preferences.preferenceDeviceId +
                    " \"" + service.preferences.preferenceDeviceName + "\"");
            BluetoothPeripheral peripheral = central.getPeripheral(service.preferences.preferenceDeviceId);
            // could we theoretically use multiple devices at once?
            central.autoConnectPeripheral(peripheral, service.getListener());
        }

        // Always return true here because we shouldn't need to retry connections
        return true;
    }
}
