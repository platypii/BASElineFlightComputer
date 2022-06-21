package com.platypii.baseline.bluetooth;

import android.location.GpsStatus;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPED;
import static com.platypii.baseline.bluetooth.BluetoothState.BT_STOPPING;

public abstract class AbstractBluetoothRunnable implements Stoppable {
    private static final String TAG = "AbstractBluetoothRunnable";

    private static final int reconnectDelay = 500; // milliseconds

    @Override
    public void run() {
        Log.i(TAG, "Bluetooth thread starting");

        // Reconnect loop
        while (getService().getState() != BT_STOPPING) {
            // Connect to bluetooth GPS
            getService().setState(BT_CONNECTING);
            final boolean isConnected = connect();
            if (getService().getState() == BT_CONNECTING && isConnected) {
                getService().setState(BT_CONNECTED);

                // Start processing NMEA sentences
                postConnect();
            }
            // Are we restarting or stopping?
            if (getService().getState() != BT_STOPPING) {
                getService().setState(BT_CONNECTING);
                // Sleep before reconnect
                try {
                    Thread.sleep(reconnectDelay);
                } catch (InterruptedException ie) {
                    Log.e(TAG, "Bluetooth thread interrupted");
                }
                Log.i(TAG, "Reconnecting to bluetooth device");
            } else {
                Log.d(TAG, "Bluetooth thread about to stop");
            }
        }

        // Bluetooth service stopped
        getService().setState(BT_STOPPED);
    }

    protected void postConnect(){
        try {
            while (getService().getState() == BT_CONNECTED) {}
        } catch (Exception e) {
            if (getService().getState() == BT_CONNECTED) {
                Log.e(TAG, "Error reading from bluetooth socket", e);
            }
        } finally {
            Log.d(TAG, "Bluetooth thread shutting down");
        }
    };

    protected abstract boolean connect();

    protected abstract AbstractBluetoothService getService();
}
