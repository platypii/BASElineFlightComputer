package com.platypii.baseline.lasers.rangefinder;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.events.RangefinderEvent;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_STARTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STATES;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Class to manage a bluetooth laser rangefinder.
 * Note: instantiating this class will not automatically start bluetooth. Call start to connect.
 */
public class RangefinderService implements BaseService {
    private static final String TAG = "RangefinderService";

    public static final int ENABLE_BLUETOOTH_CODE = 13;

    // Bluetooth state
    private int bluetoothState = BT_STOPPED;
    @Nullable
    private BluetoothAdapter bluetoothAdapter;
    @Nullable
    private RangefinderRunnable bluetoothRunnable;
    @Nullable
    private Thread bluetoothThread;

    @Override
    public void start(@NonNull Context context) {
        if (BluetoothState.started(bluetoothState)) {
            Exceptions.report(new IllegalStateException("Rangefinder started twice"));
            return;
        }
        if (!(context instanceof Activity)) {
            Exceptions.report(new ClassCastException("Rangefinder context must be an activity"));
            return;
        }
        final Activity activity = (Activity) context;
        setState(BT_STARTING);
        startAsync(activity);
    }

    /**
     * Starts bluetooth in an async thread.
     * Even though we're mostly just starting the bluetooth thread, calling getAdapter can be slow.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startAsync(@NonNull final Activity activity) {
        AsyncTask.execute(() -> {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothRunnable = new RangefinderRunnable(this, activity.getApplicationContext(), bluetoothAdapter);
                    bluetoothThread = new Thread(bluetoothRunnable);
                    bluetoothThread.start();
                } else {
                    // Turn on bluetooth
                    Log.i(TAG, "Requesting to turn on bluetooth");
                    final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_CODE);
                }
            } else {
                Log.w(TAG, "Bluetooth adapter not found");
            }
        });
    }

    /**
     * Parent activity should call this method if it detects bluetooth was enabled
     */
    public void bluetoothStarted(@NonNull Activity activity) {
        Log.i(TAG, "Bluetooth started late");
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (bluetoothRunnable == null) {
                bluetoothRunnable = new RangefinderRunnable(this, activity, bluetoothAdapter);
                bluetoothThread = new Thread(bluetoothRunnable);
                bluetoothThread.start();
            }
        } else {
            Exceptions.report(new IllegalStateException("Bluetooth supposedly started, but adapter not enabled"));
        }
    }

    public int getState() {
        return bluetoothState;
    }

    void setState(int state) {
        Log.d(TAG, "Rangefinder bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        bluetoothState = state;
        EventBus.getDefault().post(new RangefinderEvent());
    }

    @Override
    public synchronized void stop() {
        Log.i(TAG, "Stopping rangefinder service");
        if (!BluetoothState.started(bluetoothState)) {
            Log.e(TAG, "Rangefinder service not started");
        }
        setState(BT_STOPPING);
        if (bluetoothRunnable != null && bluetoothThread != null) {
            // Stop thread
            bluetoothRunnable.stop();
            try {
                bluetoothThread.join(1000);

                // Thread is dead, clean up
                bluetoothRunnable = null;
                bluetoothThread = null;
                Log.i(TAG, "Rangefinder service stopped cleanly");
                setState(BT_STOPPED);
            } catch (InterruptedException e) {
                Log.e(TAG, "Bluetooth thread interrupted while waiting for it to die", e);
            }
        }
        setState(BT_STOPPED);
    }

}
