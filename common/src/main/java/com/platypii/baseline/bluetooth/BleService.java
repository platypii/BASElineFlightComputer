package com.platypii.baseline.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.platypii.baseline.location.LocationProvider;
import com.platypii.baseline.measurements.MGravity;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.measurements.MRotation;
import com.platypii.baseline.measurements.MSensor;
import com.platypii.baseline.measurements.Measurement;
import com.platypii.baseline.measurements.GenericListener;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BleService extends AbstractBluetoothService {
    private static final String TAG = "BleService";
    private static final String RACEBOX = "RaceBox";

    private final RaceBoxListener bleListener;

    @Nullable
    private BleRunnable bleRunnable;
    public final BlePreferences preferences = new BlePreferences();
    private Map<Class<? extends Measurement>, Set<GenericListener<?>>> sensorListeners;
    private Set<DeviceScanListener> deviceListeners;
    private Set<LocationProvider> locationListeners;

    public BleService() {
        this.bleListener = new RaceBoxListener();
        this.sensorListeners = new HashMap<>();
        this.locationListeners = new HashSet<>();
        this.deviceListeners = new HashSet<>();
    }

    @Override
    protected BleRunnable getRunnable() {
        return bleRunnable;
    }

    @Override
    protected void startService(@NonNull Activity activity) {
        bleRunnable = new BleRunnable(this, activity);
        startService(bleRunnable);
    }

    @Override
    protected void stopRunnable() {
        super.stopRunnable();
        bleRunnable = null;
    }

    public void reconnect(){
        if (bleRunnable != null) {
            bleRunnable.reconnect();
        }
    }

    public BluetoothPeripheralCallback getListener() {
        return bleListener;
    }

    private <T extends MSensor> void addGenericListener(Class<T> clazz, GenericListener<T> listener) {
        sensorListeners.putIfAbsent(clazz, new HashSet<>());
        sensorListeners.get(clazz).add(listener);
    }

    private <T extends MSensor> void removeGenericListener(Class<T> clazz, GenericListener<T> listener) {
        Optional.ofNullable(sensorListeners.get(clazz)).ifPresent(set -> set.remove(listener));
    }

    public void addListener(LocationProvider locationProvider) {
        this.locationListeners.add(locationProvider);
    }

    public void removeListener(LocationProvider locationProvider) {
        this.locationListeners.remove(locationProvider);
    }

    public void addGravityListener(GenericListener gravityListener) {
        addGenericListener(MGravity.class, gravityListener);
    }

    public void addRotationListener(GenericListener rotationListener) {
        addGenericListener(MRotation.class, rotationListener);
    }

    public void removeScanListener(DeviceScanListener scanListener) {
        deviceListeners.remove(scanListener);
        if (this.deviceListeners.size() == 0 && bleRunnable != null) {
            bleRunnable.stopScan();
        }
    }

    public void addScanListener(DeviceScanListener scanListener) {
        deviceListeners.add(scanListener);
        if (bleRunnable != null) {
            bleRunnable.startScan();
        }
    }

    public void notifyScanListeners(ScanResult scanResult) {
        this.deviceListeners.forEach(listener -> listener.onScanResult(scanResult));
    }

    private class RaceBoxListener extends BluetoothPeripheralCallback {
        public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, byte[] value,
                                           @NonNull BluetoothGattCharacteristic characteristic,
                                           @NonNull GattStatus status) {
            try {
                Log.v(TAG, "Got BLE message: " + Arrays.toString(value));
                ByteBufferReader buff = new ByteBufferReader(value);
                int header = buff.getUnsignedShortAsInt();
                short clazz = buff.getByte();
                short id = buff.getByte();
                int len = buff.getUnsignedShortAsInt();
                if (len < 80) {
                    Log.d(TAG, "Probably not enough data in message: " + len);
                }
                long iotw = buff.getUnsignedIntAsLong(); // number of milliseconds from the GPS week start
                int year = buff.getUnsignedShortAsInt();
                short month = buff.getByte(); // Month indexing starts from 1 for January
                short day = buff.getByte();
                short hour = buff.getByte();
                short min = buff.getByte();
                short sec = buff.getByte();
                short validity = buff.getByte(); // bit0 - 1=valid date; bit1 - 1=valid date; bit2 - 1=fully resolved; bit3 - 1=valid magnetic declination
                long accuracy = buff.getUnsignedIntAsLong(); // time accuracy in ns
                int ns = buff.getInt(); //  Nanoseconds are signed and can be negative
                short fix_status = buff.getByte(); // 0=no fix; 2=2d fix; 3=3d fix
                short fix_status_flags = buff.getByte();
                short date_time_flags = buff.getByte(); // bit 5 - 1=available confirmation fo adate/time validity; bit6 - 1=confirmed utc date validity; bit7 - 1=confirmed utc time validity
                short num_svs = buff.getByte(); // the number of space vehicles used to compute the solution
                double longitude = buff.getInt() / Math.pow(10, 7); // coordinates of the receiver with a factor of 10^7
                double latitude = buff.getInt() / Math.pow(10, 7); // coordinates of the receiver with a factor of 10^7
                double wgs_alt = buff.getInt() / 1000.0; // Altitude in millimetres - in the coordinate system of the Ellipsoid
                double msl_alt = buff.getInt() / 1000.0; // Altitude in millimetres - an approximation of altitude above Mean Sea Level
                long horiz_acc = buff.getUnsignedIntAsLong(); // indication of the receiver’s location error in centimetres
                long vert_acc = buff.getUnsignedIntAsLong();
                int speed = buff.getInt(); // ground speed of the vehicle in millimetres per second.
                double heading = buff.getInt() / Math.pow(10, 5); // direction of motion in degrees with a factor of 10^5, where zero is North
                long speed_acc = buff.getUnsignedIntAsLong(); // estimation of the error of the Speed field in millimetres per second
                double heading_acc = buff.getUnsignedIntAsLong() / Math.pow(10, 5); // estimation of the error of the Heading fid in degrees with a factor of 10^5
                float pdop = (float) (buff.getUnsignedShortAsInt() / 100.0); // Position Dilution of Precision - indicates the error propagation of the satellite
                // configuration. Usually directly related to the number of satellites. Value is with
                // a factor  of 100
                short lat_lon_flags = buff.getByte(); // bit0- 1=invalid lat,long,wgs/msl alt; bit4..1 differential correction age
                short batt_status = buff.getByte(); // contains charging status in the most significant bit (1 if charging) and
                // estimation of the battery level in percentage in the remaining 7 bits
                double g_x = buff.getShort() / 1000.0; // acceleration on the 3 axis in milli-g
                double g_y = buff.getShort() / 1000.0;
                double g_z = buff.getShort() / 1000.0;
                double rot_x = buff.getShort() / 100.0; // speed of rotation on the 3 axis in centi- degrees per second
                double rot_y = buff.getShort() / 100.0;
                double rot_z = buff.getShort() / 100.0;
                short checksum = buff.getShort();

                ZonedDateTime dateTime = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("UTC"))
                        .plusNanos(ns); // since racebox nanos can be negative and ZonedDateTime doesn't like that
                Instant instant = dateTime.toInstant();
                long nanos = instant.getNano();
                long millis = instant.toEpochMilli();

                long epochNanos = millis * 1_000_000_000 + nanos;

                if ((lat_lon_flags & 0x01) > 0) {
                    // lat/long/alt is invalid; discard
                    Log.d(TAG, "Discarding invalid message");
                    return;
                }

                MLocation loc = new MLocation(millis,
                        latitude, longitude,
                        msl_alt, 0,
                        0, 0,
                        0, pdop, 0, 0,
                        num_svs, 0);
                Log.d(TAG, String.format("New location: %s", loc));
                BleService.this.locationListeners.forEach(listener -> listener.updateLocation(loc));

                MRotation rotation = new MRotation(0, (float) rot_x, (float) rot_y, (float) rot_z);
                notifyListeners(MRotation.class, rotation);

                MGravity gravity = new MGravity(0, (float) g_x, (float) g_y, (float) g_z);
                notifyListeners(MGravity.class, gravity);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing BLE message", e);
            }
        }
    }

    private <T extends MSensor> void notifyListeners(Class<T> clazz, T measurement) {
        this.sensorListeners.getOrDefault(clazz, Collections.emptySet())
                .forEach(listener -> ((GenericListener<T>) listener).onSensorChanged(measurement));
    }

    public void doConnect(Context context, BluetoothDevice device) {
        preferences.save(context, true, device.getAddress(), device.getName());
        if(bleRunnable != null){
            bleRunnable.reconnect();
        }
    }
}
