package com.platypii.baseline;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.MySensorManager;


public class MainActivity extends ActionBarActivity {

    public static final long startTime = System.currentTimeMillis(); // Session start time (when the app started)

    private Button startButton;
    private Button stopButton;
    private Button jumpsButton;
    private Button sensorsButton;
    private TextView clock;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int updateInterval = 32; // in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        jumpsButton = (Button) findViewById(R.id.jumpsButton);
        sensorsButton = (Button) findViewById(R.id.sensorsButton);
        clock = (TextView) findViewById(R.id.clock);

        // Start flight services
        initServices();
    }

    private void initServices() {
        // Initialize Services
        Log.i("Main", "Initializing location");
        MyLocationManager.initLocation(getApplication());
        Log.i("Main", "Initializing sensors");
        MySensorManager.initSensors(getApplication());
        Log.i("Main", "Initializing altimeter");
        MyAltimeter.initAltimeter(getApplication());
    }

    public void clickStart(View v) {
        Log.i("Main", "Starting logging");
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        jumpsButton.setEnabled(false);
        sensorsButton.setEnabled(false);

        // Start logging
        MyDatabase.startLogging(getApplicationContext());

        // Start periodic UI updates
        handler.post(new Runnable() {
            public void run() {
            update();
            handler.postDelayed(this, updateInterval);
            }
        });
    }

    public void clickStop(View v) {
        Log.i("Main", "Stopping logging");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        jumpsButton.setEnabled(true);
        sensorsButton.setEnabled(true);
        // Stop logging
        MyDatabase.stopLogging();
    }

    public void clickJumps(View v) {
        // Open jumps activity
        final Intent intent = new Intent(this, JumpsActivity.class);
        startActivity(intent);
    }

    public void clickSensors(View v) {
        // Open sensor activity
        final Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }

    private void update() {
        clock.setText(MyDatabase.getLogTime());
    }

}
