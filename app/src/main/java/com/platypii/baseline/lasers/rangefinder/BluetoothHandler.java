package com.platypii.baseline.lasers.rangefinder;

import com.platypii.baseline.Permissions;
import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_SCANNING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;
import static com.platypii.baseline.bluetooth.BluetoothUtil.byteArrayToHex;

/**
 * Thread that reads from bluetooth connection.
 * Rangefinder messages are emitted as events.
 */
class BluetoothHandler {
    private static final String TAG = "BluetoothHandler";

    @NonNull
    private final RangefinderService service;
    @NonNull
    private final Activity activity;
    @NonNull
    private final BluetoothCentralManager central;
    @Nullable
    private BluetoothPeripheral currentPeripheral;
    @Nullable
    private RangefinderProtocol protocol;

    boolean connected = false;

    BluetoothHandler(@NonNull RangefinderService service, @NonNull Activity activity, @NonNull Handler handler) {
        this.service = service;
        this.activity = activity;
        central = new BluetoothCentralManager(activity.getApplicationContext(), bluetoothCentralManagerCallback, handler);
    }

    public void start() {
        if (BluetoothState.started(service.getState())) {
            scanIfPermitted();
        } else if (service.getState() == BT_SCANNING) {
            Log.w(TAG, "Already searching");
        } else if (service.getState() == BT_STOPPING || service.getState() != BT_STOPPED) {
            Log.w(TAG, "Already stopping");
        }
    }

    private void scanIfPermitted() {
        if (Permissions.hasBluetoothPermissions(activity)) {
            Log.d(TAG, "Bluetooth permitted, starting scan");
            try {
                scan();
            } catch (SecurityException e) {
                Log.e(TAG, "Permission exception while bluetooth scanning", e);
                Exceptions.report(e);
            }
        } else {
            Log.w(TAG, "Bluetooth permission required");
            Permissions.requestBluetoothPermissions(activity);
        }
    }

    private void scan() {
        service.setState(BT_SCANNING);
        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
        Log.i(TAG, "Scanning for laser rangefinders");
        // TODO: Check for permissions
        central.scanForPeripherals(); // TODO: filter with services
    }

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
            Log.i(TAG, "Bluetooth services discovered for " + peripheral.getName());
            protocol.onServicesDiscovered();
        }

        @Override
        public void onNotificationStateUpdate(@NonNull final BluetoothPeripheral peripheral, @NonNull final BluetoothGattCharacteristic characteristic, @NonNull final GattStatus status) {
            if (status != GattStatus.SUCCESS) {
                Log.e(TAG, "Failed changing notification state for " + characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicWrite(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull final GattStatus status) {
            if (status != GattStatus.SUCCESS) {
                Log.w(TAG, "Failed writing " + byteArrayToHex(value) + " to " + characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull final GattStatus status) {
            if (status != GattStatus.SUCCESS) return;
            if (value.length == 0) return;
            processBytes(value);
        }
    };

    // Callback for central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {

        @Override
        public void onConnectedPeripheral(@NonNull BluetoothPeripheral connectedPeripheral) {
            currentPeripheral = connectedPeripheral;
            Log.i(TAG, "Rangefinder connected " + connectedPeripheral.getName());
            connected = true;
            service.setState(BT_CONNECTED);
        }

        @Override
        public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral, @NonNull final HciStatus status) {
            Log.e(TAG, "Rangefinder connection " + peripheral.getName() + " failed with status " + status);
            start(); // start over
        }

        @Override
        public void onDisconnectedPeripheral(@NonNull final BluetoothPeripheral peripheral, @NonNull final HciStatus status) {
            Log.i(TAG, "Rangefinder disconnected " + peripheral.getName() + " with status " + status);
            connected = false;
            currentPeripheral = null;
            // Go back to searching
            if (BluetoothState.started(service.getState())) {
                scanIfPermitted();
            }
        }

        @Override
        public void onDiscoveredPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull ScanResult scanResult) {
            if (service.getState() != BT_SCANNING) {
                Log.e(TAG, "Invalid BT state: " + BluetoothState.BT_STATES[service.getState()]);
            }

            // TODO: Check for bluetooth connect permission
            final ScanRecord record = scanResult.getScanRecord();
            final String deviceName = peripheral.getName();
            if (ATNProtocol.isATN(peripheral)) {
                Log.i(TAG, "ATN rangefinder found, connecting to: " + deviceName);
                connect(peripheral);
                protocol = new ATNProtocol(peripheral);
            } else if (UineyeProtocol.isUineye(peripheral, record)) {
                Log.i(TAG, "Uineye rangefinder found, connecting to: " + deviceName);
                connect(peripheral);
                protocol = new UineyeProtocol(peripheral);
            } else if (SigSauerProtocol.isSigSauer(peripheral, record)) {
                Log.i(TAG, "SigSauer rangefinder found, connecting to: " + deviceName);
                connect(peripheral);
                protocol = new SigSauerProtocol(peripheral);
            }
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Log.i(TAG, "bluetooth adapter changed state to " + state);
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                start();
            }
        }
    };

    private void connect(@NonNull BluetoothPeripheral peripheral) {
        if (service.getState() != BT_SCANNING) {
            Log.e(TAG, "Invalid BT state: " + BluetoothState.BT_STATES[service.getState()]);
        }
        central.stopScan();
        service.setState(BT_CONNECTING);
        // Connect to device
        central.connectPeripheral(peripheral, peripheralCallback);
        // TODO: Log event
    }

    private void processBytes(@NonNull byte[] value) {
        if (protocol != null) {
            protocol.processBytes(value);
        }
    }

    void stop() {
        service.setState(BT_STOPPING);
        // Stop scanning
        central.stopScan();
        if (currentPeripheral != null) {
            currentPeripheral.cancelConnection();
        }
        // Don't close central because it won't come back if we re-start
//        central.close();
        service.setState(BT_STOPPED);
    }

}