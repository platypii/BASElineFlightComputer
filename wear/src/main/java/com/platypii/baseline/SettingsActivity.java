package com.platypii.baseline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void clickBluetooth(View v) {
        // Launch bluetooth activity
        startActivity(new Intent(this, BluetoothActivity.class));
    }

}
