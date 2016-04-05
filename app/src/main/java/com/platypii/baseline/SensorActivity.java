package com.platypii.baseline;

import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.SyncedList;
import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.MySensorManager;
import com.platypii.baseline.data.measurements.MSensor;
import com.platypii.baseline.util.Util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SensorActivity extends Activity implements MyAltitudeListener, MyLocationListener {

    // Barometer
    private TextView pressureLabel;
    private TextView pressureAltitudeLabel;
    private TextView barStatsLabel;
    private TextView altitudeLabel;
    private TextView altitudeRefreshLabel;
    // GPS
    private TextView satelliteLabel;
    private TextView lastFixLabel;
    private TextView latitudeLabel;
    private TextView longitudeLabel;
    private TextView gpsAltitudeLabel;
    private TextView hAccLabel;
    private TextView pdopLabel;
    private TextView hdopLabel;
    private TextView vdopLabel;
    private TextView groundSpeedLabel;
    private TextView totalSpeedLabel;
    private TextView glideRatioLabel;
    private TextView glideAngleLabel;
    private TextView bearingLabel;

    // Sensors
    private LinearLayout sensorLayout;
    // private final ArrayList<SensorPlot> plots = new ArrayList<>();

    // Periodic UI updates    
    private final Handler handler = new Handler();
    private final int updateInterval = 100; // in milliseconds
    private Runnable updateRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_sensors);

        // Find UI elements:
        // Barometer
        pressureLabel = (TextView)findViewById(R.id.pressureLabel);
        pressureAltitudeLabel = (TextView)findViewById(R.id.pressureAltitudeLabel);
        barStatsLabel = (TextView)findViewById(R.id.barStatsLabel);
        altitudeLabel = (TextView)findViewById(R.id.altitudeLabel);
        altitudeRefreshLabel = (TextView)findViewById(R.id.altitudeRefreshLabel);

        // GPS
        satelliteLabel = (TextView)findViewById(R.id.satelliteLabel);
        lastFixLabel = (TextView)findViewById(R.id.lastFixLabel);
        latitudeLabel = (TextView)findViewById(R.id.latitudeLabel);
        longitudeLabel = (TextView)findViewById(R.id.longitudeLabel);
        gpsAltitudeLabel = (TextView)findViewById(R.id.gpsAltitudeLabel);
        hAccLabel = (TextView)findViewById(R.id.hAccLabel);
        pdopLabel = (TextView)findViewById(R.id.pdopLabel);
        hdopLabel = (TextView)findViewById(R.id.hdopLabel);
        vdopLabel = (TextView)findViewById(R.id.vdopLabel);
        groundSpeedLabel = (TextView)findViewById(R.id.groundSpeedLabel);
        totalSpeedLabel = (TextView)findViewById(R.id.totalSpeedLabel);
        glideRatioLabel = (TextView)findViewById(R.id.glideRatioLabel);
        glideAngleLabel = (TextView)findViewById(R.id.glideAngleLabel);
        bearingLabel = (TextView)findViewById(R.id.bearingLabel);

        // Sensors
        sensorLayout = (LinearLayout)findViewById(R.id.sensorLayout);
        // TextView sensorsLabel = (TextView)findViewById(R.id.sensorsLabel);
        // sensorsLabel.setText("Sensors: \n" + MySensorManager.getSensorsString());
        
        if(MySensorManager.gravity != null) {
            addPlot("Gravity", MySensorManager.gravity);
        }
        if(MySensorManager.rotation != null) {
            addPlot("Rotation", MySensorManager.rotation);
        }

        // addPlot("Magnetic", MySensorManager.getHistory(Sensor.TYPE_MAGNETIC_FIELD));
        // addPlot("Accelerometer", MySensorManager.history.get(Sensor.TYPE_ACCELEROMETER));
        // addPlot("Gyro", MySensorManager.gyroHistory, 0, 10, 100);
        // addPlot("Gyro (int)", MySensorManager.gyroHistory, 1, 10, 100);
        // addPlot("Linear Acceleration", MySensorManager.linearAccelHistory, 0, 10, 100); // Linear Acceleration = Accel - Gravity
        // addPlot("Linear Velocity", MySensorManager.linearAccelHistory, 1, 20, 100);
        // addPlot("Linear Position", MySensorManager.linearAccelHistory, 2, 90, 120);

        // Set up altimeter logging
        if(MyAltimeter.refreshRate > 0) {
            // The number of samples needed to fill the plot
            final int numSamples = (int)(Math.floor(MyAltimeter.refreshRate * ElevationTimePlot.window)) + 10;
            MyAltimeter.history.setMaxSize(numSamples);
        } else {
            MyAltimeter.history.setMaxSize(300);
        }
        // Set up sensor logging
        MySensorManager.accel.setMaxSize(300);
        MySensorManager.gravity.setMaxSize(300);
        MySensorManager.rotation.setMaxSize(300);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start GPS updates
        MyLocationManager.addListener(this);
        updateGPS(MyLocationManager.lastLoc);

        // Start altitude updates
        MyAltimeter.addListener(this);
        updateAltimeter();

        // Periodic UI updates
        updateRunnable = new Runnable() {
            public void run() {
                update();
                handler.postDelayed(this, updateInterval);
            }
        };
        handler.post(updateRunnable);
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
        updateRunnable = null;
        MyAltimeter.removeListener(this);
        MyLocationManager.removeListener(this);
    }

    private void addPlot(String label, SyncedList<MSensor> history) {
        if(history != null) {
            final TextView textView = new TextView(this);
            textView.setText(label);
            sensorLayout.addView(textView);

            final SensorPlot plot = new SensorPlot(this, null);
            plot.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 180));
            plot.loadHistory(history);

            sensorLayout.addView(plot);
            // plots.add(plot);
        }
    }

    private void updateAltimeter() {
        pressureLabel.setText("Pressure: " + Convert.pressure(MyAltimeter.pressure));
        pressureAltitudeLabel.setText("Pressure Altitude: " + Convert.distance(MyAltimeter.pressure_altitude, 2));
        barStatsLabel.setText("Pressure Stats: mean = " + Convert.distance(MyAltimeter.pressure_altitude_stat.mean(), 2) + ", stdev = " + Convert.distance(Math.sqrt(MyAltimeter.pressure_altitude_stat.var()), 2));
        altitudeLabel.setText("Altitude (corrected): " + Convert.distance(MyAltimeter.altitude, 2));
    }

    private void updateGPS(MLocation loc) {
        satelliteLabel.setText("Satellites: " + MyLocationManager.satellitesInView + " visible, " + MyLocationManager.satellitesUsed + " used in fix");
        if(loc != null) {
            if (Util.isReal(loc.latitude)) {
                latitudeLabel.setText(String.format("Lat: %.6f", loc.latitude));
            } else {
                latitudeLabel.setText("Lat: ");
            }
            if (Util.isReal(loc.latitude)) {
                longitudeLabel.setText(String.format("Long: %.6f", loc.longitude));
            } else {
                longitudeLabel.setText("Long: ");
            }
            gpsAltitudeLabel.setText("GPS Altitude: " + Convert.distance(loc.altitude_gps));
            hAccLabel.setText("hAcc: " + Convert.distance(loc.hAcc));
            pdopLabel.setText(String.format("pdop: %.1f", loc.pdop));
            hdopLabel.setText(String.format("hdop: %.1f", loc.hdop));
            vdopLabel.setText(String.format("vdop: %.1f", loc.vdop));
            groundSpeedLabel.setText("Ground speed: " + Convert.speed(loc.groundSpeed()));
            totalSpeedLabel.setText("Total speed: " + Convert.speed(loc.totalSpeed()));
            glideRatioLabel.setText("Glide ratio: " + Convert.glide(loc.glideRatio()));
            glideAngleLabel.setText("Glide angle: " + Convert.angle(loc.glideAngle()));
            bearingLabel.setText("Bearing: " + Convert.bearing2(loc.bearing()));
        }
    }

    /** Updates the UI that refresh continuously, such as sample rates */
    private void update() {
        // Last fix needs to be updated continuously since it shows time since last fix
        if(MyLocationManager.lastFixMillis > 0) {
            // Set text color
            final long lastFixDuration = MyLocationManager.lastFixDuration();
            if(lastFixDuration > 3000) {
                float frac = (6000f - lastFixDuration) / (3000f);
                frac = Math.max(0, Math.min(frac, 1));
                final int b = (int)(0xb0 * frac); // blue
                final int gb = b + 0x100 * b; // blue + green
                lastFixLabel.setTextColor(0xffb00000 + gb);
            } else {
                lastFixLabel.setTextColor(0xffb0b0b0);
            }
            String lastFix = (lastFixDuration / 1000) + "s";
            if(MyLocationManager.refreshRate > 0) {
                lastFix += String.format(" (%.2fHz)", MyLocationManager.refreshRate);
            }
            lastFixLabel.setText("Last fix: " + lastFix);
        } else {
            lastFixLabel.setTextColor(0xffb0b0b0);
            lastFixLabel.setText("Last fix: ");
        }
        // Altitude refresh rate
        altitudeRefreshLabel.setText(String.format("Sample rate: %.2fHz", MyAltimeter.refreshRate));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyAltimeter.history.setMaxSize(0);
        MySensorManager.accel.setMaxSize(0);
    }

    // Listeners
    @Override
    public void onLocationChanged(MLocation loc) {}
    @Override
    public void onLocationChangedPostExecute() {
        updateGPS(MyLocationManager.lastLoc);
    }
    @Override
    public void altitudeDoInBackground(MAltitude alt) {}
    @Override
    public void altitudeOnPostExecute() {
        updateAltimeter();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        // Stop flight services
        Services.stop();
    }
}
