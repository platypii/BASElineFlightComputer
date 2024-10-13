package com.platypii.baseline.lasers.rangefinder;

import com.platypii.baseline.bluetooth.BleException;
import com.platypii.baseline.bluetooth.BleProtocol;
import com.platypii.baseline.lasers.LaserMeasurement;
import com.platypii.baseline.util.Exceptions;

import android.bluetooth.le.ScanRecord;
import android.os.ParcelUuid;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.WriteType;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

import static com.platypii.baseline.bluetooth.BluetoothUtil.byteArrayToHex;
import static com.platypii.baseline.bluetooth.BluetoothUtil.toManufacturerString;

/**
 * This class contains ids, commands, and decoders for Vectronix Terrapin-X laser rangefinders.
 */
class TerrapinProtocol extends BleProtocol {
    private static final String TAG = "TerrapinProtocol";

    // Manufacturer ID
    private static final int manufacturerId1 = 1164;
    private static final byte[] manufacturerData1 = {1, -96, -1, -1, -1, -1, 0}; // 01-a0-ff-ff-ff-ff-00

    // Terrapin service
    private static final UUID terrapinService = UUID.fromString("81480000-b0b7-4074-8a24-ae554e5cdbc4");
    // Terrapin characteristic: read, indicate
    private static final UUID terrapinCharacteristic1 = UUID.fromString("81480100-b0b7-4074-8a24-ae554e5cdbc4");
    // Terrapin characteristic: notify, write
    private static final UUID terrapinCharacteristic2 = UUID.fromString("81480200-b0b7-4074-8a24-ae554e5cdbc4");

    private static final String factoryModeSecretKey = "b6987833";

    // Terrapin packet types
    private static final short packetTypeCommand = 0x00;
    private static final short packetTypeData = 0x03;
    private static final short packetTypeAck = 0x04;
    private static final short packetTypeNack = 0x05;

    // Terrapin commands
    private static final short commandNewMeasurementAvailable = 0x1000;
    private static final short commandStartMeasurement = 0x1001;
    private static final short commandGetLastRange = 0x1002;
    private static final short commandGetLastInclination = 0x1003;
    private static final short commandGetLastDirection = 0x1004;
    private static final short commandGetLastTemperature = 0x1005;
    private static final short commandGetLastPressure = 0x1006;
    private static final short commandGetLastEHR = 0x1007;
    private static final short commandGetLaserMode = 0x1022;
    private static final short commandGetDeclination = 0x1027;
    private static final short commandGetRangeGate = 0x1028;
    // private static final short commandGetLastSNR = 0xf006;

    // private static final short commandGetComVersion = 0x01;
    // private static final short commandGetSupportedCommandSet = 0x02;
    // private static final short commandGetSerialNumber = 0x03;
    // private static final short commandActivateFactoryMode = 0x04;
    // private static final short commandGetHardwareRevision = 0x05;
    // private static final short commandGetFirmwareVersion = 0x06;
    // private static final short commandGetBatteryLevel = 0x07;
    // private static final short commandGetDeviceName = 0x08;
    // private static final short commandSetDeviceName = 0x09;
    // private static final short commandGetDeviceId = 0x0a;

    @NonNull
    private final TerraSentenceIterator sentenceIterator = new TerraSentenceIterator();

    private short pendingCommand;
    private double lastRange;

    @Override
    public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
        try {
            // Request rangefinder service
            Log.i(TAG, "app -> rf: subscribe");
            peripheral.setNotify(terrapinService, terrapinCharacteristic1, true);
        } catch (Throwable e) {
            Log.e(TAG, "rangefinder handshake exception", e);
        }
    }

    @Override
    public void processBytes(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value) {
        // Log.d(TAG, "rf -> app: processBytes " + byteArrayToHex(value));
        sentenceIterator.addBytes(value);
        while (sentenceIterator.hasNext()) {
            processSentence(peripheral, sentenceIterator.next());
        }
    }

    private void processSentence(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value) {
        // Log.d(TAG, "rf -> app: processSentence " + byteArrayToHex(value));

        // Unescape special characters 0x7e and 0x7d
        byte[] frame = unescape(value);

        // Check checksum
        final int checksumValue = getShort(frame, frame.length - 2);
        byte[] checksumContent = Arrays.copyOfRange(frame, 0, frame.length - 2);
        final short checksumComputed = Crc16.crc16(checksumContent);
        if (checksumValue != checksumComputed) {
            Log.w(TAG, "rf -> app: invalid checksum " + byteArrayToHex(checksumContent) + " " + shortToHex(checksumValue) + " != " + shortToHex(checksumComputed));
        }

        // Packet types
        final short packetType = getShort(frame, 0);
        // Data length
        int dataLength = getShort(frame, 2);
        if (dataLength == 2) dataLength = 0;
        // Command
        final int command = getShort(frame, 4);
        if (packetType == packetTypeCommand) {
            if (dataLength != frame.length - 8) {
                Log.w(TAG, "rf -> app: invalid command data length " + dataLength + " != " + (frame.length - 8));
            }
            final byte[] data = Arrays.copyOfRange(frame, 6, frame.length - 2);
            if (command == commandNewMeasurementAvailable) {
                Log.i(TAG, "rf -> app: new measurement available");
                // Read range and inclination
                getLastRange(peripheral);
            } else {
                Log.w(TAG, "rf -> app: command unknown 0x" + shortToHex(command) + " " + dataLength + " " + byteArrayToHex(data));
            }
        } else if (packetType == packetTypeData) {
            if (dataLength != frame.length - 6) {
                Log.w(TAG, "rf -> app: invalid data length " + dataLength + " != " + (frame.length - 6));
            }
            final byte[] data = Arrays.copyOfRange(frame, 4, frame.length - 2);
            handleData(peripheral, data);
        } else if (packetType == packetTypeAck) {
            if (dataLength != frame.length - 8) {
                Log.w(TAG, "rf -> app: invalid ack data length " + dataLength + " != " + (frame.length - 8));
            }
            Log.i(TAG, "rf -> app: ack len=" + dataLength + " 0x" + shortToHex(command));
        } else if (packetType == packetTypeNack) {
            if (dataLength != frame.length - 8) {
                Log.w(TAG, "rf -> app: invalid nack data length " + dataLength + " != " + (frame.length - 8));
            }
            Log.i(TAG, "rf -> app: nack len=" + dataLength + " 0x" + shortToHex(command));
        } else {
            Log.w(TAG, "rf -> app: unknown packet type " + shortToHex(packetType) + " " + byteArrayToHex(frame));
        }
    }

    private void handleData(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] data) {
        Log.d(TAG, "rf -> app: data len=" + data.length + " " + byteArrayToHex(data));
        final short dataCommand = pendingCommand;
        pendingCommand = 0;
        if (dataCommand == commandGetLastRange) {
            if (data.length == 4) {
                final int value = (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
                final double range = (value == -2147483648) ? Double.NaN : value * 0.01; // centimeters
                Log.i(TAG, "rf -> app: last range " + range + " meters");
                lastRange = range;
                if (!Double.isNaN(range)) {
                    getLastInclination(peripheral);
                } else {
                    Log.w(TAG, "rf -> app: no range");
                }
            } else {
                Log.w(TAG, "rf -> app: invalid range data length " + data.length);
            }
        } else if (dataCommand == commandGetLastInclination) {
            if (data.length == 4) {
                final int value = (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
                final double inclination = value * 0.001;
                Log.i(TAG, "rf -> app: last inclination " + inclination + " degrees");
                final double x = lastRange * Math.cos(Math.toRadians(inclination));
                final double y = lastRange * Math.sin(Math.toRadians(inclination));
                processMeasurement(x, y);
            } else {
                Log.w(TAG, "rf -> app: invalid inclination data length " + data.length);
            }
        } else if (dataCommand == commandGetLastDirection) {
            if (data.length == 4) {
                final int value = (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
                final double direction = value * 0.001;
                Log.i(TAG, "rf -> app: last direction " + direction);
            } else {
                Log.w(TAG, "rf -> app: invalid direction data length " + data.length);
            }
        } else if (dataCommand == commandGetDeclination) {
            if (data.length == 4) {
                final int declinationTenths = (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
                final double declination = declinationTenths * 0.1;
                Log.i(TAG, "rf -> app: declination " + declination);
            } else {
                Log.w(TAG, "rf -> app: invalid declination data length " + data.length);
            }
        } else {
            Log.w(TAG, "rf -> app: no pending command for data " + " " + byteArrayToHex(data));
        }
    }

    private String shortToHex(int value) {
        return Integer.toHexString(value & 0xffff);
    }

    private void processMeasurement(double x, double y) {
        Log.i(TAG, "rf -> app: measure " + x + " " + y);
        EventBus.getDefault().post(new LaserMeasurement(x, y));
    }

    private void getLastRange(@NonNull BluetoothPeripheral peripheral) {
        Log.i(TAG, "app -> rf: get last range");
        sendCommand(peripheral, commandGetLastRange, null);
    }

    private void getLastInclination(@NonNull BluetoothPeripheral peripheral) {
        Log.i(TAG, "app -> rf: start measurement");
        sendCommand(peripheral, commandGetLastInclination, null);
    }

    private void sendCommand(@NonNull BluetoothPeripheral peripheral, short command, @Nullable byte[] data) {
        if (pendingCommand != 0) {
            Log.w(TAG, "app -> rf: command already pending " + shortToHex(pendingCommand));
        }
        pendingCommand = command;
        final int dataLength = data == null ? 0 : data.length; // TODO: / 2 ?
        byte[] frame = new byte[8 + dataLength];
        // Packet type
        frame[0] = (byte) (packetTypeCommand & 0xff);
        frame[1] = (byte) ((packetTypeCommand >> 8) & 0xff);
        // Data length
        if (data != null) {
            frame[2] = (byte) (dataLength & 0xff);
            frame[3] = (byte) ((dataLength >> 8) & 0xff);
            System.arraycopy(data, 0, frame, 6, data.length);
        } else {
            frame[2] = 2;
            frame[3] = 0;
        }
        // Command
        frame[4] = (byte) (command & 0xff);
        frame[5] = (byte) ((command >> 8) & 0xff);
        // Checksum
        final int checksum = Crc16.crc16(Arrays.copyOfRange(frame, 0, frame.length - 2));
        frame[frame.length - 1] = (byte) ((checksum >> 8) & 0xff);
        frame[frame.length - 2] = (byte) (checksum & 0xff);
        // Escape special characters 0x7e and 0x7d
        frame = escape(frame);
        // Wrap frame
        byte[] wrapped = new byte[frame.length + 2];
        wrapped[0] = 0x7e;
        System.arraycopy(frame, 0, wrapped, 1, frame.length);
        wrapped[wrapped.length - 1] = 0x7e;
        Log.d(TAG, "app -> rf: send command " + byteArrayToHex(wrapped));
        peripheral.writeCharacteristic(terrapinService, terrapinCharacteristic2, wrapped, WriteType.WITH_RESPONSE);
    }

    /**
     * Return true iff a bluetooth scan result looks like a rangefinder
     */
    @Override
    public boolean canParse(@NonNull BluetoothPeripheral peripheral, @Nullable ScanRecord record) {
        final String deviceName = peripheral.getName();
        if (record != null && Arrays.equals(record.getManufacturerSpecificData(manufacturerId1), manufacturerData1)) {
            return true; // Manufacturer match (kenny's laser)
        } else if (
                (record != null && hasRangefinderService(record))
                        || deviceName.startsWith("FastM")
                        || deviceName.startsWith("Terrapin")) {
            // Send manufacturer data to firebase
            final String mfg = toManufacturerString(record);
            Exceptions.report(new BleException("Terrapin laser unknown mfg data: " + deviceName + " " + mfg));
            return true;
        } else {
            return false;
        }
    }

    private boolean hasRangefinderService(@NonNull ScanRecord record) {
        final List<ParcelUuid> uuids = record.getServiceUuids();
        return uuids != null && uuids.contains(new ParcelUuid(terrapinService));
    }

    private byte[] escape(byte[] data) {
        final byte[] escaped = new byte[data.length * 2];
        int j = 0;
        for (byte b : data) {
            if (b == 0x7e) {
                escaped[j++] = 0x7d;
                escaped[j++] = 0x5e;
            } else if (b == 0x7d) {
                escaped[j++] = 0x7d;
                escaped[j++] = 0x5d;
            } else {
                escaped[j++] = b;
            }
        }
        return Arrays.copyOf(escaped, j);
    }

    private byte[] unescape(byte[] data) {
        final byte[] unescaped = new byte[data.length];
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x7d) {
                if (data[i + 1] == 0x5e) {
                    unescaped[j++] = 0x7e;
                } else if (data[i + 1] == 0x5d) {
                    unescaped[j++] = 0x7d;
                } else {
                    Log.w(TAG, "rf -> app: invalid escape sequence " + byteArrayToHex(data));
                }
                i++;
            } else {
                unescaped[j++] = data[i];
            }
        }
        return Arrays.copyOf(unescaped, j);
    }

    @NonNull
    private byte[] swapEndianness(@NonNull byte[] data) {
        if (data.length % 2 != 0) {
            Log.w(TAG, "rf -> app: data length must be even " + byteArrayToHex(data));
        }
        final byte[] swapped = new byte[data.length];
        for (int i = 0; i < data.length; i += 2) {
            swapped[i] = data[i + 1];
            swapped[i + 1] = data[i];
        }
        return swapped;
    }

    /**
     * Endian-swapped short from byte array
     */
    private short getShort(@NonNull byte[] data, int offset) {
        return (short) ((data[offset] & 0xff) | (data[offset + 1] << 8));
    }
}
