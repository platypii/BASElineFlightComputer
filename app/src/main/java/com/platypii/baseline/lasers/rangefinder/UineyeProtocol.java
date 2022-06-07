package com.platypii.baseline.lasers.rangefinder;

import com.platypii.baseline.bluetooth.BluetoothUtil;
import com.platypii.baseline.lasers.LaserMeasurement;
import com.platypii.baseline.views.laser.LaserActivity;

import android.bluetooth.le.ScanRecord;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.WriteType;
import java.util.Arrays;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.bluetooth.BluetoothUtil.byteArrayToHex;
import static com.platypii.baseline.bluetooth.BluetoothUtil.bytesToShort;

/**
 * This class contains ids, commands, and decoders for Uineye / Hawkeye laser rangefinders.
 */
class UineyeProtocol implements RangefinderProtocol {
    private static final String TAG = "UineyeProtocol";

    // Manufacturer ID
    private static final int manufacturerId1 = 21881;
    private static final byte[] manufacturerData1 = {-120, -96, -44, 54, 57, 101, 118, 103}; // 88-a0-d4-36-39-65-76-67

    private static final int manufacturerId2 = 19784;
    private static final byte[] manufacturerData2 = {0, 21, -123, 20, -100, 9}; // 00-15-85-14-9c-09

    // Rangefinder service
    private static final UUID rangefinderService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    // Rangefinder characteristic
    private static final UUID rangefinderCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    // Say hello to laser
    private static final byte[] appHello = {-82, -89, 4, 0, 6, 10, -68, -73};   // ae-a7-04-00-06-0a-bc-b7
    // Tell laser to shutdown. Uineye app sends this if laser doesn't say hello back in 5s.
    // private static final byte[] appGoodbye = {-82, -89, 4, 0, 7, 11, -68, -73}; // ae-a7-04-00-07-0b-bc-b7
    // Send this in response to heartbeat
    private static final byte[] appHeartbeatAck = {-82, -89, 4, 0, -120, -116, -68, -73}; // ae-a7-04-00-88-8c-bc-b7

    // Rangefinder responses
    private static final byte[] laserHello = {4, 0, -122, -118}; // ae-a7-04-00-86-8a-bc-b7
    private static final byte[] heartbeat = {4, 0, 8, 12}; // ae-a7-04-00-08-0c-bc-b7
    private static final byte[] norange = {4, 0, 5, 9}; // ae-a7-04-00-05-09-bc-b7

    // Protocol state
    @NonNull
    private final RfSentenceIterator sentenceIterator = new RfSentenceIterator();
    private final BluetoothPeripheral peripheral;

    UineyeProtocol(BluetoothPeripheral peripheral) {
        this.peripheral = peripheral;
    }

    @Override
    public void onServicesDiscovered() {
        sendHello();
        // TODO: don't sleep. correct behavior is to wait for async write completion.
        BluetoothUtil.sleep(200);
        requestRangefinderService();
    }

    @Override
    public void processBytes(@NonNull byte[] value) {
        sentenceIterator.addBytes(value);
        while (sentenceIterator.hasNext()) {
            processSentence(sentenceIterator.next());
        }
    }

    @Override
    public UUID getCharacteristic() {
        return rangefinderCharacteristic;
    }

    private void processSentence(@NonNull byte[] value) {
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
            Log.w(TAG, "rf -> app: unknown " + byteArrayToHex(value));
        }
    }

    private void requestRangefinderService() {
        Log.i(TAG, "app -> rf: subscribe");
        peripheral.setNotify(rangefinderService, rangefinderCharacteristic, true);
    }

    private void sendHello() {
        Log.d(TAG, "app -> rf: hello");
        peripheral.writeCharacteristic(rangefinderService, rangefinderCharacteristic, appHello, WriteType.WITHOUT_RESPONSE);
    }

    private void sendHeartbeatAck() {
        Log.d(TAG, "app -> rf: heartbeat ack");
        peripheral.writeCharacteristic(rangefinderService, rangefinderCharacteristic, appHeartbeatAck, WriteType.WITHOUT_RESPONSE);
    }

    private void processMeasurement(@NonNull byte[] value) {
        Log.d(TAG, "rf -> app: measure " + byteArrayToHex(value));

        final boolean metric = value[21] == 0x01;
        final double units = metric ? 1 : 0.9144; // yards or meters
        final double pitch = bytesToShort(value[3], value[4]) * 0.1 * units; // degrees
//        final double total = Util.bytesToShort(value[5], value[6]) * 0.1 * units; // meters
        double vert = bytesToShort(value[7], value[8]) * 0.1 * units; // meters
        double horiz = bytesToShort(value[9], value[10]) * 0.1 * units; // meters
//        double bearing = (value[22] & 0xff) * 360.0 / 256.0; // degrees
        if (pitch < 0) {
            vert = -vert;
        }

        final LaserMeasurement meas = new LaserMeasurement(horiz, vert);
        Log.i(TAG, "rf -> app: measure " + meas);
        EventBus.getDefault().post(meas);
    }

    /**
     * Return true iff a bluetooth scan result looks like a rangefinder
     */
    static boolean isUineye(@NonNull BluetoothPeripheral peripheral, @Nullable ScanRecord record) {
        final String deviceName = peripheral.getName();
        if (record != null && Arrays.equals(record.getManufacturerSpecificData(manufacturerId1), manufacturerData1)) {
            // Manufacturer match (kenny's laser)
            return true;
        } else if (record != null && Arrays.equals(record.getManufacturerSpecificData(manufacturerId2), manufacturerData2)) {
            // Manufacturer match (hartman's laser)
            return true;
        } else if (deviceName.endsWith("BT05")) {
            // Device name match
            if (record != null) {
                // Send manufacturer data to firebase
                final Bundle bundle = new Bundle();
                bundle.putString("rf_device_name", deviceName);
                final SparseArray<byte[]> mfg = record.getManufacturerSpecificData();
                for (int i = 0; i < mfg.size(); i++) {
                    final String key = "mfg_" + mfg.keyAt(i);
                    final String hex = byteArrayToHex(mfg.valueAt(i));
                    bundle.putString(key, hex);
                }
                LaserActivity.firebaseAnalytics.logEvent("manufacturer_data", bundle);
            }
            return true;
        } else {
            return false;
        }
    }

}
