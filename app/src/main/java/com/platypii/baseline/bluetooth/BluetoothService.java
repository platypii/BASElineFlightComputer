package com.platypii.baseline.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.GpsStatus;
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

    private static final List<GpsStatus.NmeaListener> listeners = new ArrayList<>();

    public static boolean preferenceEnabled = false;
    public static String preferenceDevice = null;

    private static boolean enabled = false;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice bluetoothDevice;
    private static BluetoothSocket bluetoothSocket;
    private static BluetoothRunnable bluetoothRunnable;

    /**
     * Start the bluetooth service, and connect to gps receiver if selected
     * @return true iff bluetooth service started successfully
     */
    public static boolean start(Activity activity) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // Device not supported
            Toast.makeText(activity, "Bluetooth not supported", Toast.LENGTH_LONG).show();
        } else if(!bluetoothAdapter.isEnabled()) {
            // Turn on bluetooth
            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_CODE);
        } else {
            Log.i(TAG, "Bluetooth is enabled, connecting...");
            enabled = connect();
        }
        return enabled;
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
                // Start listener thread to convert input stream to nmea
                if(bluetoothRunnable == null) {
                    bluetoothRunnable = new BluetoothRunnable(bluetoothSocket);
                    new Thread(bluetoothRunnable).start();
                } else {
                    Log.e(TAG, "Bluetooth listener thread already started");
                }
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
        private final BluetoothSocket bluetoothSocket;
        private boolean stop = false;
        public BluetoothRunnable(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
        }
        @Override
        public void run() {
            try {
                bluetoothSocket.connect();
                final InputStream is = bluetoothSocket.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                try {
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
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to bluetooth device", e);
            } finally {
                bluetoothRunnable = null;
            }
        }
        public void stop() {
            stop = true;
        }
    }

    public static Set<BluetoothDevice> getDevices() {
        if(enabled && bluetoothAdapter != null) {
            return bluetoothAdapter.getBondedDevices();
        } else {
            Log.w(TAG, "Tried to get devices, but bluetooth is not enabled");
            return null;
        }
    }

    public static BluetoothDevice getDevice() {
        if(enabled && bluetoothDevice != null) {
            return bluetoothDevice;
        } else {
            Log.w(TAG, "Tried to get devices, but bluetooth is not enabled");
            return null;
        }
    }

    public static void stop() {
        Log.i(TAG, "Stopping bluetooth service");
        enabled = false;
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
    }

    public static void addNmeaListener(GpsStatus.NmeaListener nmeaListener) {
        listeners.add(nmeaListener);
    }
    public static void removeNmeaListener(GpsStatus.NmeaListener nmeaListener) {
        listeners.remove(nmeaListener);
    }

}
