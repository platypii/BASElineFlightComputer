package com.platypii.baseline.bluetooth;

import android.bluetooth.le.ScanResult;

import com.welie.blessed.BluetoothPeripheral;

public interface DeviceScanListener {
    void onScanResult(ScanResult scanResult);
}
