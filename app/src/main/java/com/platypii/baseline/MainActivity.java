package com.platypii.baseline;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.KVStore;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.MySensorManager;
import com.platypii.baseline.data.TheCloud;

public class MainActivity extends Activity {

    public static final long startTime = System.currentTimeMillis(); // Session start time (when the app started)

    private Button startButton;
    private Button stopButton;
    private Button jumpsButton;
    private ImageButton signinButton;
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
        signinButton = (ImageButton) findViewById(R.id.signinButton);
        clock = (TextView) findViewById(R.id.clock);

        // Start flight services
        initServices();

        if(Auth.isAuthenticated(this)) {
            Log.i("Auth", "User signed in");
        }

        // Restore start/stop state
        updateUIState();
    }

    private void initServices() {
        // Initialize Services
        Log.i("Main", "Initializing key value store");
        KVStore.start(getApplication());
        Log.i("Main", "Initializing location");
        MyLocationManager.initLocation(getApplication());
        Log.i("Main", "Initializing sensors");
        MySensorManager.initSensors(getApplication());
        Log.i("Main", "Initializing altimeter");
        MyAltimeter.initAltimeter(getApplication());

        // TODO: Upload any unsynced files
        // TheCloud.uploadAll();
    }

    public void clickStart(View v) {
        if(!MyDatabase.isLogging()) {
            Log.i("Main", "Starting logging");

            // Start logging
            MyDatabase.startLogging(getApplicationContext());
            updateUIState();
        }
    }

    public void clickStop(View v) {
        Log.i("Main", "Stopping logging");

        // Stop logging
        final Jump jump = MyDatabase.stopLogging();
        updateUIState();

        // Upload to the cloud
        if(jump != null) {
            // Begin automatic upload
            GoogleAuth.getAuthToken(this, new Callback<String>() {
                @Override
                public void apply(String authToken) {
                    TheCloud.upload(jump, authToken, new Callback<Try<CloudData>>() {
                        @Override
                        public void apply(Try<CloudData> result) {
                            if(result instanceof Try.Success) {
                                Toast.makeText(MainActivity.this, "Track sync success", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Track sync failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } else {
            Log.e("Main", "Error reading log file");
        }
    }

    // Enables buttons and clock
    private void updateUIState() {
        if(Auth.isAuthenticated(this)) {
            signinButton.setVisibility(View.GONE);
        } else {
            signinButton.setVisibility(View.VISIBLE);
        }
        if(MyDatabase.isLogging()) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            jumpsButton.setEnabled(false);
            signinButton.setEnabled(false);

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
            signinButton.setEnabled(true);
        }
        invalidateOptionsMenu();
    }

    public void clickJumps(View v) {
        // Open jumps activity
        final Intent intent = new Intent(this, JumpsActivity.class);
        startActivity(intent);
    }

    public void clickSignIn(View v) {
        GoogleAuth.signin(this, new Callback<Boolean>() {
            @Override
            public void apply(Boolean success) {
                if(success) {
                    Toast.makeText(MainActivity.this, "Sign in successful", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Sign in failed", Toast.LENGTH_LONG).show();
                }
                updateUIState();
            }
        });
    }

    public void clickSignOut() {
        if(GoogleAuth.signout(this)) {
            Toast.makeText(this, R.string.signout_message, Toast.LENGTH_SHORT).show();
        }
        updateUIState();
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

        final MenuItem loginItem = menu.findItem(R.id.menu_item_signin);
        final MenuItem logoutItem = menu.findItem(R.id.menu_item_signout);

        // Check signin state
        if(Auth.isAuthenticated(this)) {
            // Logged in
            loginItem.setVisible(false);
            logoutItem.setVisible(true);
        } else {
            // Logged out
            loginItem.setVisible(true);
            logoutItem.setVisible(false);
        }
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
            case R.id.menu_item_signin:
                clickSignIn(null);
                return true;
            case R.id.menu_item_signout:
                clickSignOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
