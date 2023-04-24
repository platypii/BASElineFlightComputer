package com.platypii.baseline.views.bluetooth;

import android.bluetooth.le.ScanResult;
import android.os.Bundle;

import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.DeviceScanListener;
import com.platypii.baseline.databinding.ActivityBleBinding;
import com.platypii.baseline.views.BaseActivity;

public class BleActivity extends BaseActivity implements DeviceScanListener {
    private static final String TAG = "BleActivity";

    private ActivityBleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!Services.bleService.preferences.preferenceEnabled){
            Services.bleService.start(this);
        }
        Services.bleService.addScanListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Services.bleService.removeScanListener(this);
    }

    @Override
    public void onScanResult(ScanResult scanResult) {
        ((BleDeviceListFragment) this.binding.list.getFragment()).onScanResult(scanResult.getDevice());
    }

}
