package com.platypii.baseline;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.Plus;
import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.KVStore;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.MySensorManager;
import com.platypii.baseline.data.TheCloud;

public class MainActivity extends BaseActivity {
    private static final String TAG = "Main";

    public static final long startTime = System.currentTimeMillis(); // Session start time (when the app started)

    private Button startButton;
    private Button stopButton;
    private Button jumpsButton;
    private TextView clock;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int updateInterval = 32; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        jumpsButton = (Button) findViewById(R.id.jumpsButton);
        clock = (TextView) findViewById(R.id.clock);

        // Start flight services
        initServices();

        // Restore start/stop state
        updateUIState();
    }

    private void initServices() {
        // Initialize Services
        Log.i(TAG, "Initializing key value store");
        KVStore.start(getApplication());
        Log.i(TAG, "Initializing location");
        MyLocationManager.initLocation(getApplication());
        Log.i(TAG, "Initializing sensors");
        MySensorManager.initSensors(getApplication());
        Log.i(TAG, "Initializing altimeter");
        MyAltimeter.initAltimeter(getApplication());

        // TODO: Upload any unsynced files
        // TheCloud.uploadAll();
    }

    public void clickStart(View v) {
        if(!MyDatabase.isLogging()) {
            Log.i(TAG, "Starting logging");

            // Start logging
            MyDatabase.startLogging(getApplicationContext());
            updateUIState();
        }
    }

    public void clickStop(View v) {
        Log.i(TAG, "Stopping logging");

        // Stop logging
        final Jump jump = MyDatabase.stopLogging();
        updateUIState();

        // Upload to the cloud
        if(jump != null) {
            // Begin automatic upload
            getAuthToken(new Callback<String>() {
                @Override
                public void apply(String authToken) {
                    TheCloud.upload(jump, authToken, new Callback<CloudData>() {
                        @Override
                        public void apply(CloudData cloudData) {
                            Toast.makeText(MainActivity.this, "Track sync success", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void error(String error) {
                            Toast.makeText(MainActivity.this, "Track sync failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void error(String error) {
                    Toast.makeText(MainActivity.this, "Track sync failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Error reading log file");
        }
    }

    // Enables buttons and clock
    private void updateUIState() {
        if(MyDatabase.isLogging()) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            jumpsButton.setEnabled(false);

            // Start periodic UI updates
            handler.post(new Runnable() {
                public void run() {
                    updateClock();
                    if (MyDatabase.isLogging()) {
                        handler.postDelayed(this, updateInterval);
                    }
                }
            });

        } else {
            clock.setText("");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            jumpsButton.setEnabled(true);
        }
        invalidateOptionsMenu();
    }

    public void clickJumps(View v) {
        // Open jumps activity
        final Intent intent = new Intent(this, JumpsActivity.class);
        startActivity(intent);
    }

    public void clickSignOut() {
        Log.i(TAG, "Sign out");
        // Clear the default account so that GoogleApiClient will not automatically connect in the future.
        if(mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();

            // Return to SigninActivity
            final Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Sign out failed");
        }
    }

    private void updateClock() {
        if(MyDatabase.isLogging()) {
            clock.setText(MyDatabase.getLogTime());
        } else {
            clock.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return !MyDatabase.isLogging();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_sensor_info:
                // Open sensor activity
                final Intent intent = new Intent(this, SensorActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_signout:
                clickSignOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
