package com.platypii.baseline.location;

import android.content.Context;

import androidx.annotation.NonNull;

import com.platypii.baseline.bluetooth.BleService;

public class LocationProviderBLE extends LocationProvider {
    protected final String TAG = "ProviderBLE";
    private final BleService bleService;

    public LocationProviderBLE(BleService bleService) {
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
