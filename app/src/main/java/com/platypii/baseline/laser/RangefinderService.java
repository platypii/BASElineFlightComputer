package com.platypii.baseline.laser;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Exceptions;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.bluetooth.BluetoothState.*;

/**
 * Class to manage a bluetooth laser rangefinder.
 * Note: instantiating this class will not automatically start bluetooth. Call start to connect.
 */
public class RangefinderService implements BaseService {
    private static final String TAG = "RangefinderService";

    private static final int ENABLE_BLUETOOTH_CODE = 13;

    // Bluetooth state
    private int bluetoothState = BT_STOPPED;
    private BluetoothAdapter bluetoothAdapter;
    private RangefinderRunnable bluetoothRunnable;
    private Thread bluetoothThread;

    @Override
    public void start(@NonNull Context context) {
        if (!(context instanceof Activity)) {
            Exceptions.report(new ClassCastException("Rangefinder context must be an activity"));
            return;
        }
        final Activity activity = (Activity) context;
        // TODO: Check for location permission? Can't scan without location permission
        startAsync(activity);
    }

    /**
     * Starts bluetooth in an asynctask.
     * Even though we're mostly just starting the bluetooth thread, calling getAdapter can be slow.
     */
    private void startAsync(@NonNull final Activity activity) {
        AsyncTask.execute(() -> {
            bluetoothAdapter = getAdapter(activity);
            if (bluetoothAdapter != null) {
                bluetoothRunnable = new RangefinderRunnable(this, activity, bluetoothAdapter);
                bluetoothThread = new Thread(bluetoothRunnable);
                bluetoothThread.start();
            }
        });
    }

    /**
     * Start the bluetooth service, and connect to gps receiver if selected
     * @return true iff bluetooth service started successfully
     */
    @Nullable
    private BluetoothAdapter getAdapter(@NonNull Activity activity) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device not supported
            Log.e(TAG, "Bluetooth not supported");
        } else if (!bluetoothAdapter.isEnabled()) {
            // Turn on bluetooth
            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_CODE);
        }
        return bluetoothAdapter;
    }

    public int getState() {
        return bluetoothState;
    }

    void setState(int state) {
        Log.d(TAG, "Rangefinder bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        bluetoothState = state;
        EventBus.getDefault().post(new BluetoothEvent());
    }

    @Override
    public synchronized void stop() {
        Log.i(TAG, "Stopping rangefinder service");
        // Stop thread
        if (bluetoothRunnable != null) {
            bluetoothRunnable.stop();
            try {
                bluetoothThread.join(1000);

                // Thread is dead, clean up
                bluetoothRunnable = null;
                bluetoothThread = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Bluetooth thread interrupted while waiting for it to die", e);
            }
            Log.i(TAG, "Rangefinder service stopped");
        } else {
            Log.e(TAG, "Cannot stop rangefinder: runnable is null");
        }
    }

}