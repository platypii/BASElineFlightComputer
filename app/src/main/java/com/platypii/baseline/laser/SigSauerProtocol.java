package com.platypii.baseline.laser;

import com.platypii.baseline.views.laser.LaserActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.util.Arrays;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * This class contains ids, commands, and decoders for Sig Sauer laser rangefinders.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class SigSauerProtocol implements RangefinderProtocol {
    private static final String TAG = "SigSauerProtocol";

    // Manufacturer ID
    private static final int manufacturerId = 1179;
    private static final byte[] manufacturerData = {2, 0, -1, -1, -1, -1, 2, 0, -1, -1, -1, -1}; // 02-00-ff-ff-ff-ff-02-00-ff-ff-ff-ff

    // Rangefinder service
    private static final UUID rangefinderService = UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455");
    // Rangefinder characteristic
    private static final UUID rangefinderCharacteristic = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");

    // Client Characteristic Configuration (what we subscribe to)
    private static final UUID clientCharacteristicDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Protocol state
    private final BluetoothGatt bluetoothGatt;

    // Rangefinder responses
    // Commands: :AR, AI, RB, RPG1, GI, GK, SK
    // For sight: :SE, GB, RB=version
    // :AB,2,el,wd1,,range,
    // :AK = ack
    // :NA = no ack
    // :VR = rangefinder version
    // :DP,,,,,,temp,alt,ws,wd
    // :DS = rangefinder settings
    private static final String ack = ":AK";

    SigSauerProtocol(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    @Override
    public void onServicesDiscovered() {
        requestRangefinderService();
    }

    @Override
    public void processBytes(byte[] value) {
        final String str = new String(value);
        if (str.startsWith(ack)) {
            Log.d(TAG, "rf -> app: ack");
        } else if (str.startsWith(":AB,")) {
            if (validate(str)) {
                processMeasurement(str);
            } else {
                Log.e(TAG, "rf -> app: invalid checksum " + str);
            }
        } else if (str.startsWith(":DS,")) {
            Log.i(TAG, "rf -> app: goodbye " + str);
        } else {
            Log.w(TAG, "rf -> app: unknown " + str);
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

    private void processMeasurement(@NonNull String str) {
        Log.d(TAG, "rf -> app: measure " + str);
        final String[] split = str.split(",");

//        double el = Double.parseDouble(split[2]);
//        final double wd1 = Double.parseDouble(split[3]);
//        final double range = Double.parseDouble(split[5]); // total yards
//        final double energy = Double.parseDouble(split[9]);
//        final double velocity = Double.parseDouble(split[10]);
//        final double incl = Double.parseDouble(split[11]); // angle in degrees
//        if (range > 800) {
//            el = 10000;
//        }
//        Log.d(TAG, "rf -> app: measure " + el + " " + wd1 + " " + range + " " + energy + " " + velocity + " " + incl);

        double total = Double.parseDouble(split[5]) * 0.9144; // meters
        double pitch = Double.parseDouble(split[11]); // degrees

        double horiz = total * Math.cos(Math.toRadians(pitch)); // meters
        double vert = total * Math.sin(Math.toRadians(pitch)); // meters

        final LaserMeasurement meas = new LaserMeasurement(horiz, vert);
        Log.i(TAG, "rf -> app: measure " + meas);
        EventBus.getDefault().post(meas);
    }

    /**
     * Check checksum
     */
    private boolean validate(@NonNull String str) {
        int sum = 0;
        for (int i = 0; i < str.length() - 3; i++) {
            sum ^= str.charAt(i);
        }
        final String computed = String.format("%02X", sum ^ 138);
        final String given = str.substring(str.length() - 3, str.length() - 1);
        if (computed.equals(given)) {
            return true;
        } else {
            Log.e(TAG, "rf -> app: invalid checksum " + computed + " != " + given);
            return false;
        }
    }

    /**
     * Return true iff a bluetooth scan result looks like a rangefinder
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static boolean isSigSauer(@NonNull BluetoothDevice device, @Nullable ScanRecord record) {
        final String deviceName = device.getName();
        if (record != null && Arrays.equals(record.getManufacturerSpecificData(manufacturerId), manufacturerData)) {
            // Manufacturer match
            return true;
        } else if (deviceName != null && deviceName.contains("BDX")) {
            if (record != null) {
                // Send manufacturer data to firebase
                final Bundle bundle = new Bundle();
                bundle.putString("rf_device_name", deviceName);
                final SparseArray<byte[]> mfg = record.getManufacturerSpecificData();
                for (int i = 0; i < mfg.size(); i++) {
                    final String key = "mfg_" + mfg.keyAt(i);
                    final String hex = Util.byteArrayToHex(mfg.valueAt(i));
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
