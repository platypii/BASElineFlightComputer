package com.platypii.baseline.lasers.rangefinder;

import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.platypii.baseline.RequestCodes.RC_BLUE_ENABLE;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;

/**
 * Class to manage a bluetooth laser rangefinder.
 * Note: instantiating this class will not automatically start bluetooth. Call start to connect.
 */
public class RangefinderService {
    private static final String TAG = "RangefinderService";

    @Nullable
    private BluetoothAdapter bluetoothAdapter;
    @Nullable
    private BluetoothHandler bluetoothHandler;

    public void start(@NonNull Activity activity) {
        if (BluetoothState.started(getState())) {
            Exceptions.report(new IllegalStateException("Rangefinder started twice"));
            return;
        }
        Log.i(TAG, "Starting rangefinder service");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothHandler = new BluetoothHandler(activity);
                bluetoothHandler.start();
            } else {
                // Turn on bluetooth
                Log.i(TAG, "Requesting to turn on bluetooth");
                final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBluetoothIntent, RC_BLUE_ENABLE);
            }
        } else {
            Log.w(TAG, "Bluetooth adapter not found");
        }
    }

    /**
     * Parent activity should call this method if it detects bluetooth was enabled
     */
    public void bluetoothStarted(@NonNull Activity activity) {
        Log.i(TAG, "Bluetooth started late");
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (bluetoothHandler == null) {
                bluetoothHandler = new BluetoothHandler(activity);
                bluetoothHandler.start();
            }
        } else {
            Exceptions.report(new IllegalStateException("Bluetooth supposedly started, but adapter not enabled"));
        }
    }

    public int getState() {
        return bluetoothHandler != null ? bluetoothHandler.bluetoothState : BT_STOPPED;
    }

    public synchronized void stop() {
        Log.i(TAG, "Stopping rangefinder service");
        if (!BluetoothState.started(getState())) {
            Log.e(TAG, "Rangefinder service not started");
        }
        if (bluetoothHandler != null) {
            bluetoothHandler.stop();
            Log.i(TAG, "Rangefinder service stopped cleanly");
        }
    }

}
