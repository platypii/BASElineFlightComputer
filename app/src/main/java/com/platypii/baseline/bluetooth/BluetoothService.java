package com.platypii.baseline.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.GpsStatus;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.events.BluetoothEvent;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class to manage a bluetooth GPS receiver
 */
public class BluetoothService {
    private static final String TAG = "Bluetooth";

    private static final int ENABLE_BLUETOOTH_CODE = 13;

    static final List<GpsStatus.NmeaListener> listeners = new ArrayList<>();

    // Android stored references for bluetooth
    public static boolean preferenceEnabled = false;
    public static String preferenceDeviceId = null;
    public static String preferenceDeviceName = null;

    // Bluetooth finite state machine
    public static final int BT_STOPPED = 0;
    public static final int BT_CONNECTING = 1;
    public static final int BT_CONNECTED = 2;
    public static final int BT_DISCONNECTED = 3;
    public static final int BT_STOPPING = 4;
    public static final String[] BT_STATES = {"BT_STOPPED", "BT_CONNECTING", "BT_CONNECTED", "BT_DISCONNECTED", "BT_STOPPING"};
    private static int bluetoothState = BT_STOPPED;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothRunnable bluetoothRunnable;
    private static Thread bluetoothThread;

    public static void startAsync(final Activity activity) {
        if (bluetoothState != BT_STOPPED) {
            Log.e(TAG, "Bluetooth already started: " + BT_STATES[bluetoothState]);
            FirebaseCrash.report(new Exception("Bluetooth already started: " + BT_STATES[bluetoothState]));
        } else {
            BluetoothService.setState(BluetoothService.BT_CONNECTING);
            // Start bluetooth and
            // Start bluetooth thread
            if(bluetoothRunnable != null) {
                Log.e(TAG, "Bluetooth listener thread already started");
            }
            bluetoothAdapter = getAdapter(activity);
            bluetoothRunnable = new BluetoothRunnable(bluetoothAdapter);
            bluetoothThread = new Thread(bluetoothRunnable);
            bluetoothThread.start();
        }
    }

    /**
     * Start the bluetooth service, and connect to gps receiver if selected
     * @return true iff bluetooth service started successfully
     */
    private static BluetoothAdapter getAdapter(Activity activity) {
        // TODO: Make sure this doesn't take too long
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // Device not supported
            Log.e(TAG, "Bluetooth not supported");
        } else if(!bluetoothAdapter.isEnabled()) {
            // Turn on bluetooth
            // TODO: Handle result?
            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_CODE);
        }
        return bluetoothAdapter;
    }

    static Set<BluetoothDevice> getDevices() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            return bluetoothAdapter.getBondedDevices();
        } else {
            Log.w(TAG, "Tried to get devices, but bluetooth is not enabled");
            return null;
        }
    }

    public static int getState() {
        return bluetoothState;
    }

    static void setState(int state) {
        if(bluetoothState == BT_STOPPING && state == BT_CONNECTING) {
            Log.e(TAG, "Invalid bluetooth state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        Log.d(TAG, "Bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        bluetoothState = state;
        EventBus.getDefault().post(new BluetoothEvent(bluetoothState));
    }

    /**
     * Return a human-readable string for the bluetooth state
     * TODO: use string resources
     */
    public static String getStatusMessage() {
        if(isHardwareEnabled()) {
            switch(bluetoothState) {
                case BT_STOPPED:
                    return "Bluetooth stopped";
                case BT_CONNECTING:
                    return "Bluetooth connecting";
                case BT_CONNECTED:
                    return "Bluetooth connected";
                case BT_DISCONNECTED:
                    return "Bluetooth disconnected";
                case BT_STOPPING:
                    return "Bluetooth shutting down";
                default:
                    return "";
            }
        } else {
            return "Bluetooth hardware disabled";
        }
    }

    public static boolean isHardwareEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public static synchronized void stop() {
        if(bluetoothState != BT_STOPPED) {
            Log.i(TAG, "Stopping bluetooth service");
            BluetoothService.setState(BluetoothService.BT_STOPPING);
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
                Log.i(TAG, "Bluetooth service stopped");
            } else {
                Log.e(TAG, "Cannot stop bluetooth: runnable is null: " + BT_STATES[bluetoothState]);
            }
        }
    }

    public static synchronized void restart(Activity activity) {
        Log.i(TAG, "Restarting bluetooth service");
        BluetoothService.stop();
        if(bluetoothState != BT_STOPPED) {
            Log.e(TAG, "Error restarting bluetooth: not stopped: " + BT_STATES[bluetoothState]);
        }
        BluetoothService.startAsync(activity);
    }

    public static void addNmeaListener(GpsStatus.NmeaListener nmeaListener) {
        listeners.add(nmeaListener);
    }
    public static void removeNmeaListener(GpsStatus.NmeaListener nmeaListener) {
        listeners.remove(nmeaListener);
    }

}
