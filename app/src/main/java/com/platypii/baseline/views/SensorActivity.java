package com.platypii.baseline.views;

import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.ActivitySensorsBinding;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.measurements.MSensor;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.util.SyncedList;
import com.platypii.baseline.views.charts.SensorPlot;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@SuppressLint("SetTextI18n")
public class SensorActivity extends BaseActivity implements MyLocationListener {

    private ActivitySensorsBinding binding;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int updateInterval = 100; // in milliseconds
    @Nullable
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivitySensorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Sensors
        // sensorsLabel.setText("Sensors: \n" + MySensorManager.getSensorsString());

        if (Services.sensors.isEnabled()) {
            // Add plots
            addPlot("Gravity", Services.sensors.gravity);
            addPlot("Rotation", Services.sensors.rotation);

            // Increase buffer size
            Services.sensors.gravity.setMaxSize(300);
            Services.sensors.rotation.setMaxSize(300);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start GPS updates
        Services.location.addListener(this);
        updateGPS();

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

    private void addPlot(String label, @Nullable SyncedList<MSensor> history) {
        if (history != null) {
            final TextView textView = new TextView(this);
            textView.setText(label);
            binding.sensorLayout.addView(textView);

            final SensorPlot plot = new SensorPlot(this, null);
            plot.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 180));
            plot.loadHistory(history);
            binding.sensorLayout.addView(plot);
        }
    }

    private void updateAltimeter() {
        binding.altiSourceLabel.setText("Data source: " + altimeterSource());
        binding.altitudeLabel.setText("Altitude MSL: " + Convert.distance(Services.alti.altitude, 2, true));
        binding.altitudeAglLabel.setText("Altitude AGL: " + Convert.distance(Services.alti.altitudeAGL(), 2, true) + " AGL");

        binding.pressureLabel.setText(String.format(Locale.getDefault(), "Pressure: %s (%.2fHz)", Convert.pressure(Services.alti.baro.pressure), Services.alti.baro.refreshRate.refreshRate));
        binding.pressureAltitudeLabel.setText("Pressure altitude raw: " + Convert.distance(Services.alti.baro.pressure_altitude_raw, 2, true));
        if (Double.isNaN(Services.alti.baro.pressure_altitude_filtered)) {
            binding.pressureAltitudeFilteredLabel.setText("Pressure altitude filtered: ");
        } else {
            binding.pressureAltitudeFilteredLabel.setText("Pressure altitude filtered: " + Convert.distance(Services.alti.baro.pressure_altitude_filtered, 2, true) + " +/- " + Convert.distance(Math.sqrt(Services.alti.baro.model_error.var()), 2, true));
        }
        binding.fallrateLabel.setText("Fallrate: " + Convert.speed(-Services.alti.climb, 2, true));
    }

    @NonNull
    private String altimeterSource() {
        final boolean hasBaro = Services.alti.baro_sample_count > 0;
        final boolean hasGps = Services.alti.gps_sample_count > 0;
        if (hasBaro && hasGps) return "GPS + baro";
        else if (hasBaro) return "baro";
        else if (hasGps) return "GPS";
        else return "none";
    }

    private void updateGPS() {
        final MLocation loc = Services.location.lastLoc;
        if (loc != null) {
            binding.satelliteLabel.setText("Satellites: " + loc.satellitesUsed + " used in fix, " + loc.satellitesInView + " visible");
            if (Numbers.isReal(loc.latitude)) {
                binding.latitudeLabel.setText(String.format(Locale.getDefault(), "Lat: %.6f", loc.latitude));
            } else {
                binding.latitudeLabel.setText("Lat: ");
            }
            if (Numbers.isReal(loc.latitude)) {
                binding.longitudeLabel.setText(String.format(Locale.getDefault(), "Long: %.6f", loc.longitude));
            } else {
                binding.longitudeLabel.setText("Long: ");
            }
            binding.gpsAltitudeLabel.setText("GPS altitude: " + Convert.distance(loc.altitude_gps, 2, true));
            binding.gpsFallrateLabel.setText("GPS fallrate: " + Convert.speed(-Services.alti.gpsClimb(), 2, true));
            binding.hAccLabel.setText("hAcc: " + Convert.distance(loc.hAcc));
            binding.pdopLabel.setText(String.format(Locale.getDefault(), "pdop: %.1f", loc.pdop));
            binding.hdopLabel.setText(String.format(Locale.getDefault(), "hdop: %.1f", loc.hdop));
            binding.vdopLabel.setText(String.format(Locale.getDefault(), "vdop: %.1f", loc.vdop));
            binding.groundSpeedLabel.setText("Ground speed: " + Convert.speed(loc.groundSpeed(), 2, true));
            binding.totalSpeedLabel.setText("Total speed: " + Convert.speed(loc.totalSpeed(), 2, true));
            binding.glideRatioLabel.setText("Glide ratio: " + Convert.glide(loc.groundSpeed(), loc.climb, 2, true));
            binding.glideAngleLabel.setText("Glide angle: " + Convert.angle(loc.glideAngle()));
            binding.bearingLabel.setText("Bearing: " + Convert.bearing2(loc.bearing()));
            binding.flightModeLabel.setText("Flight mode: " + Services.flightComputer.getModeString());
            binding.placeLabel.setText("Location: " + Services.places.nearestPlace.getString(loc));
        }
    }

    /**
     * Updates the UI that refresh continuously, such as sample rates
     */
    private void update() {
        // Bluetooth battery level needs to be continuously updated
        if (Services.bluetooth.preferences.preferenceEnabled) {
            binding.gpsSourceLabel.setText("Data source: Bluetooth GPS");
            if (Services.bluetooth.preferences.preferenceDeviceName == null) {
                binding.bluetoothStatusLabel.setText("Bluetooth: (not selected)");
            } else {
                String status = "Bluetooth: " + Services.bluetooth.preferences.preferenceDeviceName; // TODO: Model name
                if (Services.bluetooth.charging) {
                    status += " charging";
                } else if (!Float.isNaN(Services.bluetooth.powerLevel)) {
                    final int powerLevel = (int) (Services.bluetooth.powerLevel * 100);
                    status += " " + powerLevel + "%";
                }
                binding.bluetoothStatusLabel.setText(status);
                binding.bluetoothStatusLabel.setVisibility(View.VISIBLE);
            }
        } else {
            binding.gpsSourceLabel.setText("Data source: " + Build.MANUFACTURER + " " + Build.MODEL);
            binding.bluetoothStatusLabel.setVisibility(View.GONE);
        }
        // Last fix needs to be updated continuously since it shows time since last fix
        final long lastFixDuration = Services.location.lastFixDuration();
        if (lastFixDuration >= 0) {
            // Set text color
            if (lastFixDuration > 2000) {
                // Fade from white to red linearly from 2 -> 5 seconds since last fix
                float frac = (5000f - lastFixDuration) / (3000f);
                frac = Math.max(0, Math.min(frac, 1));
                final int b = (int) (0xb0 * frac); // blue
                final int gb = b + 0x100 * b; // blue + green
                binding.lastFixLabel.setTextColor(0xffb00000 + gb);
            } else {
                binding.lastFixLabel.setTextColor(0xffb0b0b0);
            }
            String lastFix = (lastFixDuration / 1000) + "s";
            final float refreshRate = Services.location.refreshRate.refreshRate;
            if (refreshRate > 0) {
                lastFix += String.format(Locale.getDefault(), " (%.2fHz)", refreshRate);
            }
            binding.lastFixLabel.setText("Last fix: " + lastFix);
        } else {
            binding.lastFixLabel.setTextColor(0xffb0b0b0);
            binding.lastFixLabel.setText("Last fix: ");
        }
        // Altitude refresh rate
        binding.pressureLabel.setText(String.format(Locale.getDefault(), "Pressure: %s (%.2fHz)",
                Convert.pressure(Services.alti.baro.pressure), Services.alti.baro.refreshRate.refreshRate));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Services.sensors.gravity.setMaxSize(0);
        Services.sensors.rotation.setMaxSize(0);
    }

    // Listeners
    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        runOnUiThread(this::updateGPS);
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPressureEvent(MPressure alt) {
        updateAltimeter();
    }

}
