package com.platypii.baseline.laser;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * This class contains ids, commands, and decoders for ATN laser rangefinders.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ATNProtocol implements RangefinderProtocol {
    private static final String TAG = "ATNProtocol";

    // Rangefinder service
    private static final UUID rangefinderService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    // Rangefinder characteristic
    private static final UUID rangefinderCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    // Client Characteristic Configuration (what we subscribe to)
    private static final UUID clientCharacteristicDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Rangefinder responses
    // 10-01-20-00-0b-01-bb-18 // measurement
    // 10-01-a0-ff-58-01-55-b2 // measurement fail
    // 10-01-10-02-0c-00-79-68 // fog mode
    // 10-01-91-ff-58-01-bb-5c // fog mode fail yards

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
    public void processBytes(@NonNull byte[] value) {
        final String hex = Util.byteArrayToHex(value);
        if (value[0] == 16 && value[1] == 1) {
            // Check for bits we haven't seen before
            if ((value[2] & 0x4e) != 0) {
                Log.w(TAG, "Unexpected ATN command: " + hex);
            }
            final boolean success = (value[2] & 0x80) == 0;
//            final boolean normalMode = (value[2] & 0x20) != 0;
//            final boolean fogMode = (value[2] & 0x10) != 0;
//            final boolean metric = (value[2] & 0x01) == 0;

            if (success) {
                processMeasurement(value);
            } else {
                Log.i(TAG, "rf -> app: norange " + hex);
            }
        } else {
            Log.w(TAG, "rf -> app: unknown " + hex);
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

    private void processMeasurement(@NonNull byte[] value) {
        Log.d(TAG, "rf -> app: measure " + Util.byteArrayToHex(value));

        if (value[0] != 16 || value[1] != 1) {
            throw new IllegalArgumentException("Invalid measurement prefix " + Util.byteArrayToHex(value));
        }

        double total = Util.bytesToShort(value[3], value[4]) * 0.5; // meters
        double pitch = Util.bytesToShort(value[5], value[6]) * 0.1; // degrees

        double horiz = total * Math.cos(Math.toRadians(pitch)); // meters
        double vert = total * Math.sin(Math.toRadians(pitch)); // meters

        if (horiz < 0) {
            throw new IllegalArgumentException("Invalid horizontal distance " + total + " " + pitch + " " + horiz + " " + vert);
        }

        final LaserMeasurement meas = new LaserMeasurement(horiz, vert);
        Log.i(TAG, "rf -> app: measure " + meas);
        EventBus.getDefault().post(meas);
    }

    /**
     * Return true iff a bluetooth scan result looks like a rangefinder
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static boolean isATN(@NonNull BluetoothDevice device) {
        return "ATN-LD99".equals(device.getName());
    }

}
