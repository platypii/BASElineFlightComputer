package com.platypii.baseline.lasers.rangefinder;

import com.platypii.baseline.Permissions;
import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.HciStatus;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_SCANNING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STARTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STATES;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

/**
 * BLE handler for scanning and connecting to bluetooth LE devices.
 */
class BluetoothHandler {
    private static final String TAG = "BluetoothHandler";

    // Bluetooth state
    int bluetoothState = BT_STOPPED;

    @Nullable
    private Activity activity;
    @NonNull
    private final BluetoothCentralManager central;
    @NonNull
    private final BleProtocol[] protocols = {
            new UineyeProtocol(),
            new ATNProtocol(),
            new SigSauerProtocol()
    };
    @Nullable
    private BluetoothPeripheral currentPeripheral;

    BluetoothHandler(@NonNull Activity activity) {
        this.activity = activity;
        central = new BluetoothCentralManager(activity.getApplicationContext(), bluetoothCentralManagerCallback, new Handler());
    }

    public void start() {
        setState(BT_STARTING);
        scanIfPermitted();
    }

    /**
     * Check if bluetooth permissions are granted, and then scan().
     */
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

    /**
     * Scan for bluetooth peripherals
     */
    private void scan() {
        setState(BT_SCANNING);
        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
        Log.i(TAG, "Scanning for BLE peripherals");
        // TODO: Check for permissions
        central.scanForPeripherals(); // TODO: filter with services
    }

    // Callback for bluetooth central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {

        @Override
        public void onConnectedPeripheral(@NonNull BluetoothPeripheral peripheral) {
            currentPeripheral = peripheral;
            Log.i(TAG, "BLE connected " + peripheral.getName());
            setState(BT_CONNECTED);
        }

        @Override
        public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral, @NonNull final HciStatus status) {
            Log.e(TAG, "BLE connection " + peripheral.getName() + " failed with status " + status);
            start(); // start over
        }

        @Override
        public void onDisconnectedPeripheral(@NonNull final BluetoothPeripheral peripheral, @NonNull final HciStatus status) {
            Log.i(TAG, "BLE disconnected " + peripheral.getName() + " with status " + status);
            currentPeripheral = null;
            // Go back to searching
            if (BluetoothState.started(bluetoothState)) {
                scanIfPermitted();
            }
        }

        @Override
        public void onDiscoveredPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull ScanResult scanResult) {
            if (bluetoothState != BT_SCANNING) {
                Log.e(TAG, "Invalid BLE state: " + BT_STATES[bluetoothState]);
            }

            // TODO: Check for bluetooth connect permission
            final ScanRecord record = scanResult.getScanRecord();
            final String deviceName = peripheral.getName();
            for (BleProtocol protocol : protocols) {
                if (protocol.canParse(peripheral, record)) {
                    Log.i(TAG, protocol + " device found, connecting to: " + deviceName);
                    connect(peripheral, protocol);
                }
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

    private void connect(@NonNull BluetoothPeripheral peripheral, @NonNull BleProtocol protocol) {
        if (bluetoothState != BT_SCANNING) {
            Log.e(TAG, "Invalid BLE state: " + BT_STATES[bluetoothState]);
        }
        central.stopScan();
        setState(BT_CONNECTING);
        // Connect to device
        central.connectPeripheral(peripheral, protocol);
        // TODO: Log event
    }

    private void setState(int state) {
        Log.d(TAG, "BLE bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        bluetoothState = state;
        EventBus.getDefault().post(new BluetoothEvent());
    }

    void stop() {
        setState(BT_STOPPING);
        // Stop scanning
        central.stopScan();
        if (currentPeripheral != null) {
            // Central cancel does more checks
            central.cancelConnection(currentPeripheral);
        }
        // Don't close central because it won't come back if we re-start
//        central.close();
        setState(BT_STOPPED);
    }
}
