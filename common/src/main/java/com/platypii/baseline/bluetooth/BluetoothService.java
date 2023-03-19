package com.platypii.baseline.bluetooth;

import com.platypii.baseline.common.R;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.location.NMEA;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.PubSub;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.RequestCodes.RC_BLUE_ENABLE;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STARTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STATES;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Class to manage a bluetooth GPS receiver.
 * Note: instantiating this class will not automatically start bluetooth. Call startAsync to connect.
 */
public class BluetoothService {
    private static final String TAG = "Bluetooth";

    public PubSub<NMEA> nmeaUpdates = new PubSub<>();

    // Android shared preferences for bluetooth
    public final BluetoothPreferences preferences = new BluetoothPreferences();

    // Bluetooth state
    private int bluetoothState = BT_STOPPED;
    @Nullable
    private BluetoothAdapter bluetoothAdapter;
    @Nullable
    private Stoppable bluetoothRunnable;
    @Nullable
    private Thread bluetoothThread;

    // Bluetooth device battery level
    public float powerLevel = Float.NaN;
    public boolean charging = false;

    public void start(@NonNull Activity activity) {
        if (BluetoothState.started(bluetoothState)) {
            Exceptions.report(new IllegalStateException("Bluetooth started twice " + BT_STATES[bluetoothState]));
            return;
        }
        if (bluetoothState == BT_STOPPED) {
            setState(BT_STARTING);
            // Start bluetooth thread
            if (bluetoothRunnable != null) {
                Log.e(TAG, "Bluetooth thread already started");
            }
            startAsync(activity);
        } else {
            Exceptions.report(new IllegalStateException("Bluetooth already started: " + BT_STATES[bluetoothState]));
        }
    }

    /**
     * Starts bluetooth in an asynctask.
     * Even though we're mostly just starting the bluetooth thread, calling getAdapter can be slow.
     */
    private void startAsync(@NonNull final Activity activity) {
        AsyncTask.execute(() -> {
            bluetoothAdapter = getAdapter(activity);
            if (bluetoothAdapter != null) {
                bluetoothRunnable = new BluetoothRunnable(BluetoothService.this, bluetoothAdapter);
                bluetoothThread = new Thread(bluetoothRunnable);
                bluetoothThread.start();
            }
        });
    }

    /**
     * Get bluetooth adapter, request bluetooth if needed
     */
    @Nullable
    private BluetoothAdapter getAdapter(@NonNull Activity activity) {
        // TODO: Make sure this doesn't take too long
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device not supported
            Log.e(TAG, "Bluetooth not supported");
        } else if (!bluetoothAdapter.isEnabled()) {
            // Turn on bluetooth
            // TODO: Handle result?
            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBluetoothIntent, RC_BLUE_ENABLE);
        }
        return bluetoothAdapter;
    }

    /**
     * Return list of bonded devices, with GPS devices first
     */
    @NonNull
    public List<BluetoothDevice> getDevices() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            final Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
            final List<BluetoothDevice> devices = new ArrayList<>(deviceSet);
            Collections.sort(devices, new BluetoothDeviceComparator());
            return devices;
        } else {
            Log.w(TAG, "Tried to get devices, but bluetooth is not enabled");
            return new ArrayList<>();
        }
    }

    public int getState() {
        return bluetoothState;
    }

    void setState(int state) {
        Log.d(TAG, "Bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        if (bluetoothState == BT_STOPPING && state == BT_CONNECTING) {
            Log.e(TAG, "Invalid bluetooth state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        if (bluetoothState == state && state != BT_CONNECTING) {
            // Only allowed self-transition is connecting -> connecting
            Log.e(TAG, "Null state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        if (bluetoothState != state) {
            bluetoothState = state;
            EventBus.getDefault().post(new BluetoothEvent());
        }
    }

    /**
     * Return a string with the gps location device name
     */
    @NonNull
    public String getDeviceName() {
        if (!preferences.preferenceEnabled) {
            return "Phone";
        } else if (preferences.preferenceDeviceName != null) {
            return preferences.preferenceDeviceName;
        } else {
            return "";
        }
    }

    public boolean isEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Return a human-readable string for the bluetooth state
     */
    @NonNull
    public String getStatusMessage(@NonNull Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return "Bluetooth permission required";
//            }
        }
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            // Hardware disabled
            return context.getString(R.string.bluetooth_status_disabled);
        } else if (!preferences.preferenceEnabled) {
            // Bluetooth preference disabled
            return context.getString(R.string.bluetooth_status_disabled);
        } else if (preferences.preferenceDeviceId == null) {
            // Bluetooth preference enabled, but device not selected
            return context.getString(R.string.bluetooth_status_not_selected);
        } else {
            switch (bluetoothState) {
                case BT_STOPPED:
                    return context.getString(R.string.bluetooth_status_stopped);
                case BT_STARTING:
                    return context.getString(R.string.bluetooth_status_starting);
                case BT_CONNECTING:
                    return context.getString(R.string.bluetooth_status_connecting);
                case BT_CONNECTED:
                    return context.getString(R.string.bluetooth_status_connected);
                case BT_STOPPING:
                    return context.getString(R.string.bluetooth_status_stopping);
                default:
                    return "";
            }
        }
    }

    public synchronized void stop() {
        if (bluetoothState != BT_STOPPED) {
            Log.i(TAG, "Stopping bluetooth service");
            // Stop thread
            if (bluetoothRunnable != null && bluetoothThread != null) {
                bluetoothRunnable.stop();
                try {
                    bluetoothThread.join(1000);

                    // Thread is dead, clean up
                    bluetoothRunnable = null;
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

    /**
     * Restart bluetooth.
     * If bluetooth is stopped, just start it.
     */
    public synchronized void restart(@NonNull Activity activity) {
        Log.i(TAG, "Restarting bluetooth service");
        if (bluetoothState != BT_STOPPED) {
            // Stop first
            stop();
            if (bluetoothState != BT_STOPPED) {
                Exceptions.report(new IllegalStateException("Error restarting bluetooth: not stopped: " + BT_STATES[bluetoothState]));
            }
        }
        start(activity);
    }
}
