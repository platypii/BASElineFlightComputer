package com.platypii.baseline.bluetooth;

import com.platypii.baseline.location.LocationCheck;
import com.platypii.baseline.location.NMEAException;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.PubSub;

import android.bluetooth.le.ScanRecord;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.welie.blessed.BluetoothPeripheral;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class MohawkProtocol extends BleProtocol {
    private static final String TAG = "MohawkProtocol";
    private final PubSub<MLocation> locationUpdates;

    // Mohawk service
    private static final UUID mohawkService = UUID.fromString("ba5e0001-da9b-4622-b128-1e4f5022ab01");
    // Mohawk characteristic
    private static final UUID mohawkCharacteristic = UUID.fromString("ba5e0002-ad0c-4fe2-af23-55995ce8eb02");

    public MohawkProtocol(@NonNull PubSub<MLocation> locationUpdates) {
        this.locationUpdates = locationUpdates;
    }

    @Override
    public boolean canParse(@NonNull BluetoothPeripheral peripheral, @Nullable ScanRecord record) {
        // TODO: Check address
        return peripheral.getName().equals("Mohawk");
    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
        // Subscribe to mohawk service
        Log.i(TAG, "Mohawk subscribe");
        peripheral.setNotify(mohawkService, mohawkCharacteristic, true);
    }

    @Override
    public void processBytes(@NonNull BluetoothPeripheral peripheral, byte[] value) {
        if (value[0] == 'L' && value.length == 20) {
            // Unpack location
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            // Three least significant bytes of tenths since epoch
            final long time1 = ((long) value[1]) & 0xff;
            final long time2 = ((long) value[2]) & 0xff;
            final long time3 = ((long) value[3]) & 0xff;
            final long tenths = (time1 << 16) + (time2 << 8) + time3;
            final long lsb = tenths * 100;
            long now = System.currentTimeMillis();
            // Use most significant bits from system
            // Check for boundary conditions
            long checkbit = 1 << 23;
            if ((now & checkbit) > 0 && (lsb & checkbit) == 0) {
                now += checkbit;
            }
            if ((now & checkbit) == 0 && (lsb & checkbit) > 0) {
                now -= checkbit;
            }
            final long shift = 100 << 24;
            final long millis = now / shift * shift + lsb;
            final double lat = buf.getInt(4) * 1e-6; // microdegrees
            final double lng = buf.getInt(8) * 1e-6; // microdegrees
            final double alt = getShort(buf, 12) * 0.1 + 3176.8; // decimeters
            final double vN = getShort(buf, 14) * 0.01; // cm/s
            final double vE = getShort(buf, 16) * 0.01; // cm/s
            final double climb = getShort(buf, 18) * 0.01; // cm/s

            final int locationError = LocationCheck.validate(lat, lng);
            if (locationError == LocationCheck.VALID) {
                final MLocation loc = new MLocation(
                        millis, lat, lng, alt, climb, vN, vE,
                        Float.NaN, Float.NaN, Float.NaN, Float.NaN, -1, -1
                );
                Log.i(TAG, "mohawk -> app: gps " + loc);
                // Update listeners
                locationUpdates.post(loc);
            } else {
                Log.w(TAG, LocationCheck.message[locationError] + ": " + lat + "," + lng);
                Exceptions.report(new NMEAException(LocationCheck.message[locationError] + ": " + lat + "," + lng));
            }
        } else {
            Log.w(TAG, "mohawk -> app: unknown " + new String(value));
        }
    }

    private double getShort(ByteBuffer buf, int index) {
        final short shorty = buf.getShort(index);
        if (shorty == Short.MAX_VALUE) return Double.NaN;
        else return shorty;
    }
}
