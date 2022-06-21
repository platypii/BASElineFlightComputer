package com.platypii.baseline.bluetooth;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STARTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STATES;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Exceptions;

import org.greenrobot.eventbus.EventBus;

public abstract class AbstractBluetoothService {
    private static final String TAG = "AbstractBluetoothService";

    // Bluetooth state
    protected int bluetoothState = BT_STOPPED;

    @Nullable
    private Thread bluetoothThread;

    public void start(@NonNull Activity activity) {
        if (BluetoothState.started(bluetoothState)) {
            Exceptions.report(new IllegalStateException("Bluetooth started twice " + BT_STATES[getState()]));
            return;
        }

        if (getState() == BT_STOPPED) {
            setState(BT_STARTING);
            // Start bluetooth thread
            if (getRunnable() != null) {
                Log.e(TAG, "Bluetooth thread already started");
            }
            startService(activity);
        } else {
            Exceptions.report(new IllegalStateException("Bluetooth already started: " + BT_STATES[bluetoothState]));
        }
    }

    protected abstract Stoppable getRunnable();

    public void setState(int state) {
        Log.d(TAG, "Bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        if (bluetoothState == BT_STOPPING && BluetoothState.started(state)) {
            Log.e(TAG, "Invalid bluetooth state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        if (bluetoothState == state && state != BT_CONNECTING) {
            // Only allowed self-transition is connecting -> connecting
            Log.e(TAG, "Null state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        bluetoothState = state;
        EventBus.getDefault().post(new BluetoothEvent());
    }

    public int getState() {
        return bluetoothState;
    }

    /**
     * Starts bluetooth in an asynctask.
     * Even though we're mostly just starting the bluetooth thread, calling getAdapter can be slow.
     */
    protected abstract void startService(@NonNull final Activity activity);

    protected void startService(@NonNull Stoppable runnable){
        bluetoothThread = new Thread(runnable);
        bluetoothThread.start();
    }

    public void stop() {
        if (bluetoothState != BT_STOPPED) {
            Log.i(TAG, "Stopping bluetooth service");
            // Stop thread
            if (getRunnable() != null && bluetoothThread != null) {
                stopRunnable();
                try {
                    bluetoothThread.join(1000);

                    // Thread is dead, clean up
                    bluetoothThread = null;
                    if (bluetoothState != BT_STOPPED) {
                        Log.e(TAG, "Unexpected bluetooth state: state should be STOPPED when thread has stopped");
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Bluetooth thread interrupted while waiting for it to die", e);
                }
                Log.i(TAG, "Bluetooth service stopped");
            } else {
                Log.e(TAG, "Cannot stop bluetooth: runnable is null: " + BT_STATES[bluetoothState]);
                // Set state to stopped since it prevents getting stuck in state STOPPING
            }
            setState(BT_STOPPED);
        }
    }

    protected void stopRunnable() {
        getRunnable().stop();
    }
}
