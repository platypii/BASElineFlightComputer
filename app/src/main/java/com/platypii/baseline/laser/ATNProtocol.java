package com.platypii.baseline.laser;

import android.bluetooth.*;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * This class contains ids, commands, and decoders for ATN laser rangefinders.
 */
class ATNProtocol implements RangefinderProtocol {
    private static final String TAG = "ATNProtocol";

    // Rangefinder service
    private static final UUID rangefinderService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    // Rangefinder characteristic
    private static final UUID rangefinderCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    // Client Characteristic Configuration (what we subscribe to)
    private static final UUID clientCharacteristicDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Rangefinder responses
    private static final String norangePrefix = "10-01-a1-ff-58-";

    // Protocol state
    private final BluetoothGatt bluetoothGatt;

    ATNProtocol(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    @Override
    public void onServicesDiscovered() {
        requestRangefinderService();
    }

    @Override
    public void processBytes(byte[] value) {
        final String hex = Util.byteArrayToHex(value);
        if (hex.startsWith(norangePrefix)) {
            Log.i(TAG, "rf -> app: norange " + hex);
        } else if (value[0] == 16 && value[1] == 1) {
            processMeasurement(value);
        } else {
            Log.i(TAG, "rf -> app: data " + hex);
        }
    }

    @Override
    public UUID getCharacteristic() {
        return rangefinderCharacteristic;
    }

    private void requestRangefinderService() {
        Log.i(TAG, "app -> rf: subscribe");
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            // Enables notification locally:
            bluetoothGatt.setCharacteristicNotification(ch, true);
            // Enables notification on the device
            final BluetoothGattDescriptor descriptor = ch.getDescriptor(clientCharacteristicDescriptor);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private void processMeasurement(byte[] value) {
        Log.d(TAG, "rf -> app: measure " + Util.byteArrayToHex(value));

        if (value[0] != 16 || value[1] != 1) {
            throw new IllegalArgumentException("Invalid measurement prefix " + Util.byteArrayToHex(value));
        }

        double total = Util.bytesToShort(value[3], value[4]) * 0.5; // meters
        double pitch = Util.bytesToShort(value[5], value[6]) * 0.1; // degrees

        double vert = total * Math.sin(Math.toRadians(pitch)); // meters
        double horiz = total * Math.cos(Math.toRadians(pitch)); // meters

        final LaserMeasurement meas = new LaserMeasurement(pitch, total, vert, horiz);
        Log.i(TAG, "rf -> app: measure " + meas);
        EventBus.getDefault().post(meas);
    }

    /**
     * Return true iff a bluetooth scan result looks like a rangefinder
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static boolean isATN(BluetoothDevice device) {
        return "ATN-LD99".equals(device.getName());
    }

}
