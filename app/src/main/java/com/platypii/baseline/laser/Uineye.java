package com.platypii.baseline.laser;

import android.util.Log;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * This class contains ids, commands, and decoders for the Uineye / Hawkeye laser rangefinders.
 */
class Uineye {
    private static final String TAG = "RangefinderUineye";

    // Manufacturer ID
    static final int manufacturerId = 21881;
    static final byte[] manufacturerData = {-120, -96, -44, 54, 57, 101, 118, 103};

    // Rangefinder service
    static final UUID rangefinderService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    // Rangefinder characteristic
    static final UUID rangefinderCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    // Descriptors
    // Client Characteristic Configuration
    static final UUID clientCharacteristicDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Say hello to laser
    static final byte[] appHello = {-82, -89, 4, 0, 6, 10, -68, -73};   // ae-a7-04-00-06-0a-bc-b7
    // Tell laser to shutdown. Uineye app sends this if laser doesn't say hello back in 5s.
    static final byte[] appGoodbye = {-82, -89, 4, 0, 7, 11, -68, -73}; // ae-a7-04-00-07-0b-bc-b7
    // Send this in response to heartbeat
    static final byte[] appHeartbeatAck = {-82, -89, 4, 0, -120, -116, -68, -73}; // ae-a7-04-00-88-8c-bc-b7

    // Rangefinder responses
    static final byte[] laserHello = {4, 0, -122, -118}; // ae-a7-04-00-86-8a-bc-b7
    static final byte[] heartbeat = {4, 0, 8, 12};       // ae-a7-04-00-08-0c-bc-b7
    static final byte[] norange = {4, 0, 5, 9};         // ae-a7-04-00-05-09-bc-b7

    static void processMeasurement(byte[] value) {
        Log.d(TAG, "rf -> app: measure " + Util.byteArrayToHex(value));

        if (value[0] != 23 || value[1] != 0 || value[2] != -123) {
            throw new IllegalArgumentException("Invalid measurement prefix " + Util.byteArrayToHex(value));
        }

        double pitch = bytesToShort(value[3], value[4]) * 0.1; // degrees
        double total = bytesToShort(value[5], value[6]) * 0.1; // meters
        double vert = bytesToShort(value[7], value[8]) * 0.1; // meters
        double horiz = bytesToShort(value[9], value[10]) * 0.1; // meters

        if (pitch < 0) {
            vert = -vert;
        }

        // TODO: Check checksum?
//        byte checksum = value[22];

        final LaserMeasurement meas = new LaserMeasurement(pitch, total, vert, horiz);
        Log.i(TAG, "rf -> app: measure " + meas);
        EventBus.getDefault().post(meas);
    }

    private static short bytesToShort(byte b1, byte b2) {
        return (short) (((b1 & 0xff) << 8) | (b2 & 0xff));
    }

}
