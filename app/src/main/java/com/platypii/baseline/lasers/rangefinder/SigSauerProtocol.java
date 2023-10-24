package com.platypii.baseline.lasers.rangefinder;

import com.platypii.baseline.bluetooth.BleException;
import com.platypii.baseline.bluetooth.BleProtocol;
import com.platypii.baseline.lasers.LaserMeasurement;
import com.platypii.baseline.util.Exceptions;

import android.bluetooth.le.ScanRecord;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.WriteType;
import java.util.Arrays;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.bluetooth.BluetoothUtil.toManufacturerString;

/**
 * This class contains ids, commands, and decoders for Sig Sauer laser rangefinders.
 */
class SigSauerProtocol extends BleProtocol {
    private static final String TAG = "SigSauerProtocol";

    // Manufacturer ID
    private static final int manufacturerId = 1179;
    private static final byte[] manufacturerData = {2, 0, -1, -1, -1, -1, 2, 0, -1, -1, -1, -1}; // 02-00-ff-ff-ff-ff-02-00-ff-ff-ff-ff

    // Kilo 2200 BDX, device name K2200BDX-015157
    // private static final byte[] manufacturerData2 = {}; // 02-88-ff-ff-ff-ff-02-88-ff-ff-ff-ff-0f-00-00
    // From NRF: 9b-04-02-88-ff-ff-ff-ff-0f-00-00
    // Company: nVisti, LLC <0x049B>
    // 0x288FFFFFFFF0F0000

    // Rangefinder service
    private static final UUID rangefinderService = UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455");
    // Rangefinder characteristic
    private static final UUID rangefinderCharacteristic1 = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static final UUID rangefinderCharacteristic2 = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");

    // Rangefinder commands
    // Sight: :S3=dump :SE=settings :SU=sub :E3S=echo :GB=brightness :RB=version
    // :AB,2,el,wd1,,range,
    // :AI
    // :AK = ack
    // :AR = artificial range
    // :AX = latitude
    // :CP = current profile
    // :DP,,,,,,temp,alt,ws,wd = profile data
    // :DS = rangefinder settings
    // :DU = device unlock
    // :EG = get environment
    // :ES = secondary effects
    // :GI = ???
    // :GK = ???
    // :HB = wifi heartbeat
    // :MR = manual range
    // :NA = no ack (:NAK)
    // :VR = rangefinder version
    // :PN = display pin
    // :RA = reticle alignment
    // :RB = ???
    // :RPG1 = ???
    // :SK = license key
    // :VR = ack for DD
    // :WM = wind meter
    // :PC = set profile checksum
    private static final String ack = ":AK";

    @Override
    public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
        // Request rangefinder service
        Log.i(TAG, "app -> rf: subscribe");
        peripheral.setNotify(rangefinderService, rangefinderCharacteristic2, true);

        // Send unlock code
        // This is required for later gen SigSauer lasers, and will fail silently for old ones
        final String deviceName = peripheral.getName();
        final String du = appendChecksum(":DU," + deviceUnlockCode(deviceName));
        Log.i(TAG, "app -> rf: unlock " + deviceName);
        peripheral.writeCharacteristic(rangefinderService, rangefinderCharacteristic1, du.getBytes(), WriteType.WITHOUT_RESPONSE);
    }

    @Override
    public void processBytes(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value) {
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

        final double total = Double.parseDouble(split[5]) * 0.9144; // meters
        final double pitch = Double.parseDouble(split[11]); // degrees

        final double horiz = total * Math.cos(Math.toRadians(pitch)); // meters
        final double vert = total * Math.sin(Math.toRadians(pitch)); // meters

        if (horiz != 0 || vert != 0) {
            final LaserMeasurement meas = new LaserMeasurement(horiz, vert);
            Log.i(TAG, "rf -> app: measure " + meas);
            EventBus.getDefault().post(meas);
        }
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
    @Override
    public boolean canParse(@NonNull BluetoothPeripheral peripheral, @Nullable ScanRecord record) {
        final String deviceName = peripheral.getName();
        if (record != null && Arrays.equals(record.getManufacturerSpecificData(manufacturerId), manufacturerData)) {
            // Manufacturer match
            return true;
        } else if (deviceName.contains("BDX")) {
            if (record != null) {
                // Send manufacturer data to firebase
                final String mfg = toManufacturerString(record);
                Exceptions.report(new BleException("SigSauer laser unknown mfg data: " + deviceName + " " + mfg));
            }
            return true;
        } else {
            return false;
        }
    }

    private static int deviceUnlockCode(@NonNull String str) {
        int sum = 0;
        for (byte b : str.getBytes()) {
            sum += (b + sum + sum % 12 * 13 + 2) * 432;
        }
        return Math.abs(sum * 3);
    }

    private static String appendChecksum(@NonNull String str) {
        int sum = 0;
        for (char ch : str.toCharArray()) {
            sum ^= ch;
        }
        return String.format("%s,%02X\r", str, sum ^ 13 ^ 171);
    }

}
