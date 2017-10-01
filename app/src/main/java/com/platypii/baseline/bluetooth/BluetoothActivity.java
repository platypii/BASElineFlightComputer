package com.platypii.baseline.bluetooth;

import com.platypii.baseline.BaseActivity;
import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.BluetoothEvent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BluetoothActivity extends BaseActivity {
    private static final String TAG = "BluetoothActivity";

    private FirebaseAnalytics firebaseAnalytics;

    private Switch bluetoothSwitch;
    private TextView bluetoothStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        bluetoothSwitch = findViewById(R.id.bluetooth_switch);
        bluetoothStatus = findViewById(R.id.bluetooth_status);
    }

    private void updateViews() {
        bluetoothSwitch.setChecked(Services.bluetooth.preferenceEnabled);
        bluetoothStatus.setText(Services.bluetooth.getStatusMessage(this));
    }

    public void clickEnable(View v) {
        // Start or stop bluetooth
        Services.bluetooth.preferenceEnabled = !Services.bluetooth.preferenceEnabled;
        if(Services.bluetooth.preferenceEnabled) {
            Log.i(TAG, "User clicked bluetooth enable");
            firebaseAnalytics.logEvent("bluetooth_enabled", null);
            Services.bluetooth.start(this);
        } else {
            Log.i(TAG, "User clicked bluetooth disable");
            firebaseAnalytics.logEvent("bluetooth_disabled", null);
            Services.bluetooth.stop();
        }
        // Save preference
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("bluetooth_enabled", Services.bluetooth.preferenceEnabled);
        edit.apply();
        updateViews();
    }

    public void clickPair(View v) {
        Intents.openBluetoothSettings(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listen for bluetooth updates
        EventBus.getDefault().register(this);
        updateViews();
    }
    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        updateViews();
    }

}
