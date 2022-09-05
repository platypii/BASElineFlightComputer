package com.platypii.baseline.location;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.location.GpsStatus;
import android.util.Log;

import com.platypii.baseline.bluetooth.BleService;
import com.platypii.baseline.measurements.MLocation;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

import androidx.annotation.NonNull;

public class LocationProviderRaceBox extends LocationProvider {
    protected final String TAG = "ProviderRaceBox";
    private final BleService bleService;

    public LocationProviderRaceBox(BleService bleService) {
        this.bleService = bleService;
    }

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    @NonNull
    @Override
    protected String dataSource() {
        return "RaceBox";
    }

    @Override
    public void start(@NonNull Context context) {
        bleService.addListener(this);
    }

    @Override
    public void stop() {
        super.stop();
        bleService.removeListener(this);
    }
}
