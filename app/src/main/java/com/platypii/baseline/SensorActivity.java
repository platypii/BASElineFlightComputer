package com.platypii.baseline;

import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.measurements.MSensor;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.util.SyncedList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.Locale;

@SuppressLint("SetTextI18n")
public class SensorActivity extends Activity implements MyLocationListener {

    // Altimeter
    private TextView sourceLabel;
    private TextView altitudeLabel;
    private TextView altitudeAglLabel;
    private TextView groundLevelLabel;
    // Barometer
    private TextView pressureLabel;
    private TextView pressureAltitudeLabel;
    private TextView pressureAltitudeFilteredLabel;
    private TextView fallrateLabel;
    // GPS
    private TextView satelliteLabel;
    private TextView lastFixLabel;
    private TextView latitudeLabel;
    private TextView longitudeLabel;
    private TextView gpsAltitudeLabel;
    private TextView gpsFallrateLabel;
    private TextView hAccLabel;
    private TextView pdopLabel;
    private TextView hdopLabel;
    private TextView vdopLabel;
    private TextView groundSpeedLabel;
    private TextView totalSpeedLabel;
    private TextView glideRatioLabel;
    private TextView glideAngleLabel;
    private TextView bearingLabel;
    // Misc
    private TextView flightModeLabel;

    // Sensors
    private LinearLayout sensorLayout;
    // private final ArrayList<SensorPlot> plots = new ArrayList<>();

    // Periodic UI updates    
    private final Handler handler = new Handler();
    private final int updateInterval = 100; // in milliseconds
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_sensors);

        // Find UI elements:
        // Altimeter
        sourceLabel = (TextView)findViewById(R.id.sourceLabel);
        altitudeLabel = (TextView)findViewById(R.id.altitudeLabel);
        altitudeAglLabel = (TextView)findViewById(R.id.altitudeAglLabel);
        groundLevelLabel = (TextView)findViewById(R.id.groundLevelLabel);

        // Barometer
        pressureLabel = (TextView)findViewById(R.id.pressureLabel);
        pressureAltitudeLabel = (TextView)findViewById(R.id.pressureAltitudeLabel);
        pressureAltitudeFilteredLabel = (TextView)findViewById(R.id.pressureAltitudeFilteredLabel);
        fallrateLabel = (TextView)findViewById(R.id.fallrateLabel);

        // GPS
        satelliteLabel = (TextView)findViewById(R.id.satelliteLabel);
        lastFixLabel = (TextView)findViewById(R.id.lastFixLabel);
        latitudeLabel = (TextView)findViewById(R.id.latitudeLabel);
        longitudeLabel = (TextView)findViewById(R.id.longitudeLabel);
        gpsAltitudeLabel = (TextView)findViewById(R.id.gpsAltitudeLabel);
        gpsFallrateLabel = (TextView)findViewById(R.id.gpsFallrateLabel);
        hAccLabel = (TextView)findViewById(R.id.hAccLabel);
        pdopLabel = (TextView)findViewById(R.id.pdopLabel);
        hdopLabel = (TextView)findViewById(R.id.hdopLabel);
        vdopLabel = (TextView)findViewById(R.id.vdopLabel);
        groundSpeedLabel = (TextView)findViewById(R.id.groundSpeedLabel);
        totalSpeedLabel = (TextView)findViewById(R.id.totalSpeedLabel);
        glideRatioLabel = (TextView)findViewById(R.id.glideRatioLabel);
        glideAngleLabel = (TextView)findViewById(R.id.glideAngleLabel);
        bearingLabel = (TextView)findViewById(R.id.bearingLabel);

        // Misc
        flightModeLabel = (TextView) findViewById(R.id.flightModeLabel);

        // Sensors
        sensorLayout = (LinearLayout)findViewById(R.id.sensorLayout);
        // TextView sensorsLabel = (TextView)findViewById(R.id.sensorsLabel);
        // sensorsLabel.setText("Sensors: \n" + MySensorManager.getSensorsString());
        
        if(Services.sensors.gravity != null) {
            addPlot("Gravity", Services.sensors.gravity);
        }
        if(Services.sensors.rotation != null) {
            addPlot("Rotation", Services.sensors.rotation);
        }

        // addPlot("Magnetic", MySensorManager.getHistory(Sensor.TYPE_MAGNETIC_FIELD));
        // addPlot("Accelerometer", MySensorManager.history.get(Sensor.TYPE_ACCELEROMETER));
        // addPlot("Gyro", MySensorManager.gyroHistory, 0, 10, 100);
        // addPlot("Gyro (int)", MySensorManager.gyroHistory, 1, 10, 100);
        // addPlot("Linear Acceleration", MySensorManager.linearAccelHistory, 0, 10, 100); // Linear Acceleration = Accel - Gravity
        // addPlot("Linear Velocity", MySensorManager.linearAccelHistory, 1, 20, 100);
        // addPlot("Linear Position", MySensorManager.linearAccelHistory, 2, 90, 120);

        // Set up sensor logging
        Services.sensors.accel.setMaxSize(300);
        Services.sensors.gravity.setMaxSize(300);
        Services.sensors.rotation.setMaxSize(300);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start GPS updates
        Services.location.addListener(this);
        updateGPS(Services.location.lastLoc);

        // Start altitude updates
        EventBus.getDefault().register(this);
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
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
        updateRunnable = null;
        EventBus.getDefault().unregister(this);
        Services.location.removeListener(this);
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
        sourceLabel.setText("Data source: " + altimeterSource());
        altitudeLabel.setText("Altitude (gps corrected): " + Convert.distance(Services.alti.altitude, 2, true));
        altitudeAglLabel.setText("Altitude AGL: " + Convert.distance(Services.alti.altitudeAGL(), 2, true) + " AGL");
        groundLevelLabel.setText("Ground level: " + Convert.distance(Services.alti.groundLevel(), 2, true) + " pressure alt");

        pressureLabel.setText(String.format(Locale.getDefault(), "Pressure: %s (%.2fHz)", Convert.pressure(Services.alti.baro.pressure), Services.alti.baro.refreshRate));
        pressureAltitudeLabel.setText("Pressure altitude raw: " + Convert.distance(Services.alti.baro.pressure_altitude_raw, 2, true));
        if(Double.isNaN(Services.alti.baro.pressure_altitude_filtered)) {
            pressureAltitudeFilteredLabel.setText("Pressure altitude filtered: ");
        } else {
            pressureAltitudeFilteredLabel.setText("Pressure altitude filtered: " + Convert.distance(Services.alti.baro.pressure_altitude_filtered, 2, true) + " +/- " + Convert.distance(Math.sqrt(Services.alti.baro.model_error.var()), 2, true));
        }
        fallrateLabel.setText("Fallrate: " + Convert.speed(-Services.alti.climb, 2, true));
    }

    private String altimeterSource() {
        final boolean hasBaro = Services.alti.baro_sample_count > 0;
        final boolean hasGps = Services.alti.gps_sample_count > 0;
        if(hasBaro && hasGps) return "GPS + baro";
        else if(hasBaro) return "baro";
        else if(hasGps) return "GPS";
        else return "none";
    }

    private void updateGPS(MLocation loc) {
        if(loc != null) {
            satelliteLabel.setText("Satellites: " + loc.satellitesUsed + " used in fix, " + loc.satellitesInView + " visible");
            if (Numbers.isReal(loc.latitude)) {
                latitudeLabel.setText(String.format(Locale.getDefault(), "Lat: %.6f", loc.latitude));
            } else {
                latitudeLabel.setText("Lat: ");
            }
            if (Numbers.isReal(loc.latitude)) {
                longitudeLabel.setText(String.format(Locale.getDefault(), "Long: %.6f", loc.longitude));
            } else {
                longitudeLabel.setText("Long: ");
            }
            gpsAltitudeLabel.setText("GPS altitude: " + Convert.distance(loc.altitude_gps, 2, true));
            gpsFallrateLabel.setText("GPS fallrate: " + Convert.speed(-Services.alti.gpsClimb(), 2, true));
            hAccLabel.setText("hAcc: " + Convert.distance(loc.hAcc));
            pdopLabel.setText(String.format(Locale.getDefault(), "pdop: %.1f", loc.pdop));
            hdopLabel.setText(String.format(Locale.getDefault(), "hdop: %.1f", loc.hdop));
            vdopLabel.setText(String.format(Locale.getDefault(), "vdop: %.1f", loc.vdop));
            groundSpeedLabel.setText("Ground speed: " + Convert.speed(loc.groundSpeed(), 2, true));
            totalSpeedLabel.setText("Total speed: " + Convert.speed(loc.totalSpeed(), 2, true));
            glideRatioLabel.setText("Glide ratio: " + Convert.glide(loc.groundSpeed(), loc.climb, 2, true));
            glideAngleLabel.setText("Glide angle: " + Convert.angle(loc.glideAngle()));
            bearingLabel.setText("Bearing: " + Convert.bearing2(loc.bearing()));
            flightModeLabel.setText("Flight mode: " + Services.flightComputer.getModeString());
        }
    }

    /** Updates the UI that refresh continuously, such as sample rates */
    private void update() {
        // Last fix needs to be updated continuously since it shows time since last fix
        final long lastFixDuration = Services.location.lastFixDuration();
        if(lastFixDuration >= 0) {
            // Set text color
            if(lastFixDuration > 2000) {
                // Fade from white to red linearly from 2 -> 5 seconds since last fix
                float frac = (5000f - lastFixDuration) / (3000f);
                frac = Math.max(0, Math.min(frac, 1));
                final int b = (int)(0xb0 * frac); // blue
                final int gb = b + 0x100 * b; // blue + green
                lastFixLabel.setTextColor(0xffb00000 + gb);
            } else {
                lastFixLabel.setTextColor(0xffb0b0b0);
            }
            String lastFix = (lastFixDuration / 1000) + "s";
            if(Services.location.refreshRate > 0) {
                lastFix += String.format(Locale.getDefault(), " (%.2fHz)", Services.location.refreshRate);
            }
            lastFixLabel.setText("Last fix: " + lastFix);
        } else {
            lastFixLabel.setTextColor(0xffb0b0b0);
            lastFixLabel.setText("Last fix: ");
        }
        // Altitude refresh rate
        pressureLabel.setText(String.format(Locale.getDefault(), "Pressure: %s (%.2fHz)", Convert.pressure(Services.alti.baro.pressure), Services.alti.baro.refreshRate));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Services.sensors.accel.setMaxSize(0);
    }

    // Listeners
    @Override
    public void onLocationChanged(@NonNull MLocation loc) {}
    @Override
    public void onLocationChangedPostExecute() {
        updateGPS(Services.location.lastLoc);
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPressureEvent(MPressure alt) {
        updateAltimeter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Stop flight services
        Services.stop();
    }
}
