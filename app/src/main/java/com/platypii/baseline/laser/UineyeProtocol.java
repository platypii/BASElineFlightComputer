package com.platypii.baseline.laser;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.Arrays;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * This class contains ids, commands, and decoders for Uineye / Hawkeye laser rangefinders.
 */
class UineyeProtocol implements RangefinderProtocol {
    private static final String TAG = "UineyeProtocol";

    // Manufacturer ID
    private static final int manufacturerId = 21881;
    private static final byte[] manufacturerData = {-120, -96, -44, 54, 57, 101, 118, 103};

    // Rangefinder service
    private static final UUID rangefinderService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    // Rangefinder characteristic
    private static final UUID rangefinderCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    // Client Characteristic Configuration (what we subscribe to)
    private static final UUID clientCharacteristicDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Say hello to laser
    private static final byte[] appHello = {-82, -89, 4, 0, 6, 10, -68, -73};   // ae-a7-04-00-06-0a-bc-b7
    // Tell laser to shutdown. Uineye app sends this if laser doesn't say hello back in 5s.
    private static final byte[] appGoodbye = {-82, -89, 4, 0, 7, 11, -68, -73}; // ae-a7-04-00-07-0b-bc-b7
    // Send this in response to heartbeat
    private static final byte[] appHeartbeatAck = {-82, -89, 4, 0, -120, -116, -68, -73}; // ae-a7-04-00-88-8c-bc-b7

    // Rangefinder responses
    private static final byte[] laserHello = {4, 0, -122, -118}; // ae-a7-04-00-86-8a-bc-b7
    private static final byte[] heartbeat = {4, 0, 8, 12};       // ae-a7-04-00-08-0c-bc-b7
    private static final byte[] norange = {4, 0, 5, 9};         // ae-a7-04-00-05-09-bc-b7

    // Protocol state
    private final RfSentenceIterator sentenceIterator = new RfSentenceIterator();
    private final BluetoothGatt bluetoothGatt;

    UineyeProtocol(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    @Override
    public void onServicesDiscovered() {
        sendHello();
        Util.sleep(200); // TODO: Is this needed?
        requestRangefinderService();
    }

    @Override
    public void processBytes(byte[] value) {
        sentenceIterator.addBytes(value);
        while (sentenceIterator.hasNext()) {
            processSentence(sentenceIterator.next());
        }
    }

    @Override
    public UUID getCharacteristic() {
        return rangefinderCharacteristic;
    }

    private void processSentence(byte[] value) {
        if (Arrays.equals(value, laserHello)) {
            Log.i(TAG, "rf -> app: hello");
        } else if (Arrays.equals(value, heartbeat)) {
            Log.d(TAG, "rf -> app: heartbeat");
            sendHeartbeatAck();
        } else if (Arrays.equals(value, norange)) {
            Log.i(TAG, "rf -> app: norange");
        } else if (value[0] == 23 && value[1] == 0) {
            processMeasurement(value);
        } else {
            Log.i(TAG, "rf -> app: data " + Util.byteArrayToHex(value));
        }
    }

    private void requestRangefinderService() {
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

    private void sendHello() {
        Log.d(TAG, "app -> rf: hello");
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            ch.setValue(appHello);
            bluetoothGatt.writeCharacteristic(ch);
        }
    }

    private void sendHeartbeatAck() {
        Log.d(TAG, "app -> rf: heartbeat ack");
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            ch.setValue(appHeartbeatAck);
            bluetoothGatt.writeCharacteristic(ch);
        }
    }

    private void processMeasurement(byte[] value) {
        Log.d(TAG, "rf -> app: measure " + Util.byteArrayToHex(value));

        if (value[0] != 23 || value[1] != 0 || value[2] != -123) {
            throw new IllegalArgumentException("Invalid measurement prefix " + Util.byteArrayToHex(value));
        }

        double pitch = Util.bytesToShort(value[3], value[4]) * 0.1; // degrees
        double total = Util.bytesToShort(value[5], value[6]) * 0.1; // meters
        double vert = Util.bytesToShort(value[7], value[8]) * 0.1; // meters
        double horiz = Util.bytesToShort(value[9], value[10]) * 0.1; // meters

        if (pitch < 0) {
            vert = -vert;
        }

        // TODO: Check checksum?
//        byte checksum = value[22];

        final LaserMeasurement meas = new LaserMeasurement(pitch, total, vert, horiz);
        Log.i(TAG, "rf -> app: measure " + meas);
        EventBus.getDefault().post(meas);
    }

    /**
     * Return true iff a bluetooth scan result looks like a rangefinder
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static boolean isUineye(ScanRecord record) {
        return record != null && Arrays.equals(record.getManufacturerSpecificData(manufacturerId), manufacturerData);
    }

}
