package com.platypii.baseline.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.GpsStatus;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Class to manage a bluetooth GPS receiver
 */
public class BluetoothService {
    private static final String TAG = "Bluetooth";
    private static final int ENABLE_BLUETOOTH_CODE = 13;
    private static final UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int reconnectDelay = 5 * 1000; // 5 seconds

    private static final List<GpsStatus.NmeaListener> listeners = new ArrayList<>();

    public static boolean preferenceEnabled = false;
    public static String preferenceDevice = null;

    private static boolean isEnabled = false;
    public static boolean isConnecting = false;
    public static boolean isConnected = false;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice bluetoothDevice;
    private static BluetoothSocket bluetoothSocket;
    private static BluetoothRunnable bluetoothRunnable;

    public static void startAsync(final Activity activity) {
        if(isEnabled || isConnecting || isConnecting) {
            Log.e(TAG, "Bluetooth already enabled, or in the process of connecting");
        }
        isEnabled = true;
        isConnecting = true;
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground (Void...params){
                Log.i(TAG, "Starting bluetooth service");
                if (BluetoothService.preferenceEnabled) {
                    isConnected = BluetoothService.start(activity);
                    isConnecting = false;
                }
                return null;
            }
        }.execute();
    }

    /**
     * Start the bluetooth service, and connect to gps receiver if selected
     * @return true iff bluetooth service started successfully
     */
    private static boolean start(Activity activity) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // Device not supported
            Toast.makeText(activity, "Bluetooth not supported", Toast.LENGTH_LONG).show();
            return false;
        } else if(!bluetoothAdapter.isEnabled()) {
            // Turn on bluetooth
            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_CODE);
            return false;
        } else {
            Log.i(TAG, "Bluetooth is enabled, connecting...");
            return connect();
        }
    }

    /**
     * Connect to gps receiver
     * @return true iff bluetooth socket was created successfully
     */
    private static boolean connect() {
        if(preferenceDevice != null) {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(preferenceDevice);
            final UUID uuid;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                uuid = bluetoothDevice.getUuids()[0].getUuid();
            } else {
                uuid = DEFAULT_UUID;
            }
            Log.i(TAG, "Connecting to bluetooth device: " + bluetoothDevice.getName());
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                // Start listener thread to convert input stream to nmea
                if(bluetoothRunnable != null) {
                    Log.w(TAG, "Bluetooth listener thread already started");
                }
                bluetoothRunnable = new BluetoothRunnable();
                new Thread(bluetoothRunnable).start();
                return true;
            } catch(IOException e) {
                Log.e(TAG, "Exception connecting to bluetooth device", e);
                return false;
            }
        } else {
            Log.e(TAG, "Cannot connect: bluetooth device not selected");
            return false;
        }
    }

    /**
     * Thread that reads from bluetooth input stream, and turns into NMEA sentences
     */
    private static class BluetoothRunnable implements Runnable {
        private boolean stop = false;
        @Override
        public void run() {
            Log.i(TAG, "Bluetooth thread starting");
            try {
                final InputStream is = bluetoothSocket.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while(!stop && (line = reader.readLine()) != null) {
                    final String nmea = line.trim();
                    // Log.v(TAG, "Got line: " + nmea);
                    // Update listeners
                    for(GpsStatus.NmeaListener listener : listeners) {
                        listener.onNmeaReceived(System.currentTimeMillis(), nmea);
                    }
                }
            } catch (IOException e) {
                if(!stop) {
                    Log.e(TAG, "Error reading from bluetooth socket", e);
                    isConnected = false;

                    // Reconnect
                    while(!stop && !isConnected && !isConnecting) {
                        try {
                            Thread.sleep(reconnectDelay);
                            Log.i(TAG, "Attempting to reconnect to bluetooth device");
                            isConnecting = true;
                            isConnected = connect();
                            isConnecting = false;
                        } catch (InterruptedException ie) {
                            Log.e(TAG, "Bluetooth thread interrupted");
                        }
                    }
                }
            } finally {
                bluetoothRunnable = null;
                Log.v(TAG, "Bluetooth thread shutting down");
            }
        }
        public void stop() {
            stop = true;
        }
    }

    public static Set<BluetoothDevice> getDevices() {
        if(isEnabled && bluetoothAdapter != null) {
            return bluetoothAdapter.getBondedDevices();
        } else {
            Log.w(TAG, "Tried to get devices, but bluetooth is not enabled");
            return null;
        }
    }

    public static BluetoothDevice getDevice() {
        if(isEnabled && bluetoothDevice != null) {
            return bluetoothDevice;
        } else {
            Log.w(TAG, "Tried to get devices, but bluetooth is not enabled");
            return null;
        }
    }

    public static void stop() {
        Log.i(TAG, "Stopping bluetooth service");
        // Stop thread
        if(bluetoothRunnable != null) {
            bluetoothRunnable.stop();
        }
        // Close bluetooth socket
        if(bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.w(TAG, "Exception closing bluetooth socket", e);
            }
        }
        isConnected = false;
        isEnabled = false;
    }

    public static void addNmeaListener(GpsStatus.NmeaListener nmeaListener) {
        listeners.add(nmeaListener);
    }
    public static void removeNmeaListener(GpsStatus.NmeaListener nmeaListener) {
        listeners.remove(nmeaListener);
    }

}
