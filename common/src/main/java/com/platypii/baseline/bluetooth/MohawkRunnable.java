package com.platypii.baseline.bluetooth;

import com.platypii.baseline.events.BluetoothEvent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STARTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Thread that reads from bluetooth
 */
class MohawkRunnable implements Stoppable {
    private static final String TAG = "MohawkRunnable";

    @NonNull
    final BluetoothService service;
    @NonNull
    private final BluetoothAdapter bluetoothAdapter;
    private Context context;
    @Nullable
    private BluetoothGatt bluetoothGatt;
    @Nullable
    private BluetoothDevice mohawkDevice;

    @NonNull
    private final BluetoothGattCallback mohawkCallback;

    // Bluetooth state
    int bluetoothState = BT_STOPPED;

    MohawkRunnable(@NonNull BluetoothService service, @NonNull BluetoothAdapter bluetoothAdapter, Context context) {
        this.service = service;
        this.bluetoothAdapter = bluetoothAdapter;
        this.context = context;
        mohawkCallback = new MohawkGattCallback(this);
    }

    @Override
    public void run() {
        Log.i(TAG, "Mohawk thread starting");
        setState(BT_STARTING);
        // Connect directly
        mohawkDevice = bluetoothAdapter.getRemoteDevice(service.preferences.preferenceDeviceId);
        connect();
        // TODO: this whole run() is fast, probably shouldn't even be a Runnable
        Log.i(TAG, "MohawkRunnable finished");
    }

    /**
     * Connect to gps receiver.
     * Precondition: bluetooth enabled and preferenceDeviceId != null
     */
    private void connect() {
        if (mohawkDevice != null) {
            setState(BT_CONNECTING);
            bluetoothGatt = mohawkDevice.connectGatt(context, true, mohawkCallback);
        } else {
            Log.e(TAG, "Cannot connect to null device");
        }
    }

    /**
     * Called when connection is broken, but we should probably reconnect
     */
    void onDisconnect() {
        if (bluetoothState == BT_CONNECTED) {
            // Try to reconnect (scanning doesn't seem to work?)
            connect();
        } else {
            Log.i(TAG, "Mohawk disconnected");
        }
    }

    void setState(int state) {
        bluetoothState = state;
        EventBus.getDefault().post(new BluetoothEvent());
    }

    @Override
    public void stop() {
        Log.i(TAG, "Stopping mohawk connection");
        // Close bluetooth connection
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }
        setState(BT_STOPPING);
    }
}
