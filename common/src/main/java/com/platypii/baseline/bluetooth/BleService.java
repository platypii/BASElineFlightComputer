package com.platypii.baseline.bluetooth;

import com.platypii.baseline.Permissions;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.HciStatus;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.RequestCodes.RC_BLUE_ENABLE;
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
public class BleService {
    private static final String TAG = "BleService";

    // Bluetooth state
    public int bluetoothState = BT_STOPPED;

    @Nullable
    private Activity activity;
    @Nullable
    private BluetoothCentralManager central;

    private final Handler handler = new Handler();

    @NonNull
    private final BleProtocol[] protocols;

    @Nullable
    private BluetoothPeripheral currentPeripheral;

    public BleService(@NonNull BleProtocol... protocols) {
        this.protocols = protocols;
    }

    public void start(@NonNull Activity activity) {
        this.activity = activity;
        if (BluetoothState.started(bluetoothState)) {
            Exceptions.report(new IllegalStateException("BLE started twice"));
            return;
        }
        Log.i(TAG, "Starting BLE service");
        setState(BT_STARTING);
        if (activity == null) {
            Exceptions.report(new NullPointerException("activity should not be null"));
            return;
        }
        central = new BluetoothCentralManager(activity.getApplicationContext(), bluetoothCentralManagerCallback, handler);

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            scanIfPermitted(activity);
        } else {
            // Turn on bluetooth
            Log.i(TAG, "Requesting to turn on bluetooth");
            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBluetoothIntent, RC_BLUE_ENABLE);
        }
    }

    /**
     * Check if bluetooth permissions are granted, and then scan().
     */
    private void scanIfPermitted(@NonNull Activity activity) {
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
        Log.i(TAG, "Scanning for laser rangefinders");
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
            start(activity); // start over
        }

        @Override
        public void onDisconnectedPeripheral(@NonNull final BluetoothPeripheral peripheral, @NonNull final HciStatus status) {
            Log.i(TAG, "BLE disconnected " + peripheral.getName() + " with status " + status);
            currentPeripheral = null;
            // Go back to searching
            if (BluetoothState.started(bluetoothState)) {
                scanIfPermitted(activity);
            }
        }

        @Override
        public void onDiscoveredPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull ScanResult scanResult) {
            if (bluetoothState != BT_SCANNING) {
                Log.e(TAG, "Invalid BLE state: " + BluetoothState.BT_STATES[bluetoothState]);
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
                start(activity);
            }
        }
    };

    private void connect(@NonNull BluetoothPeripheral peripheral, @NonNull BleProtocol protocol) {
        if (bluetoothState != BT_SCANNING) {
            Log.e(TAG, "Invalid BLE state: " + BluetoothState.BT_STATES[bluetoothState]);
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

    public void stop() {
        // Stop scanning
        if (bluetoothState == BT_SCANNING) {
            central.stopScan();
        }
        setState(BT_STOPPING);
        if (currentPeripheral != null) {
            // Central cancel does more checks
            if (central != null) {
                central.cancelConnection(currentPeripheral);
            } else {
                currentPeripheral.cancelConnection();
            }
        }
        // Don't close central because it won't come back if we re-start
//        central.close();
        activity = null;
        setState(BT_STOPPED);
    }
}
