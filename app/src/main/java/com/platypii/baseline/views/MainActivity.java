package com.platypii.baseline.views;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.ActivityMainBinding;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.tracks.ImportCSV;
import com.platypii.baseline.views.altimeter.AltimeterActivity;
import com.platypii.baseline.views.laser.LaserActivity;
import com.platypii.baseline.views.map.MapActivity;
import com.platypii.baseline.views.tracks.TrackListActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity {

    // app start time
    // public static final long startTimeMillis = System.currentTimeMillis();
    // public static final long startTimeNano = System.nanoTime();

    private ActivityMainBinding binding;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int clockUpdateInterval = 48; // milliseconds
    @Nullable
    private Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // enableStrictMode();

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Import CSV if app was opened from file browser
        doImport(getIntent());

        binding.record.setOnClickListener(this::clickRecord);
        binding.audible.setOnClickListener(this::clickAudible);
        binding.audible.setOnLongClickListener(audibleLongClickListener);
        binding.alti.setOnClickListener(this::clickAltimeter);
        binding.nav.setOnClickListener(this::clickNav);
        binding.tracks.setOnClickListener(this::clickTracks);
        binding.profiles.setOnClickListener(this::clickLasers);
        binding.flysight.setOnClickListener(this::clickFlysight);
        binding.settings.setOnClickListener(this::clickSettings);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        doImport(intent);
    }

    private void doImport(@NonNull Intent intent) {
        final boolean importing = ImportCSV.importIntent(this, intent);
        if (importing) {
            firebaseAnalytics.logEvent("import_csv", null);
            Toast.makeText(this, "Importing CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Restore start/stop state
        updateUIState();

        // Listen for event updates
        EventBus.getDefault().register(this);
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                // .detectAll()
                // .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    public void clickRecord(View v) {
        final Bundle bundle = new Bundle();
        if (Services.location.lastLoc != null) {
            bundle.putFloat("lat", (float) Services.location.lastLoc.latitude);
            bundle.putFloat("lon", (float) Services.location.lastLoc.longitude);
        }
        if (!Services.tracks.logger.isLogging()) {
            firebaseAnalytics.logEvent("click_logging_start", bundle);
            Services.tracks.logger.startLogging();
        } else {
            firebaseAnalytics.logEvent("click_logging_stop", bundle);
            Services.tracks.logger.stopLogging();
        }
    }

    // Enables buttons and clock
    private void updateUIState() {
        final boolean logging = Services.tracks.logger.isLogging();
        // Update record button state
        binding.record.setText(logging ? R.string.action_stop : R.string.action_record);
        binding.record.setCompoundDrawablesWithIntrinsicBounds(0, logging ? R.drawable.square : R.drawable.circle, 0, 0);
        // Update clock state
        if (logging) {
            // Start clock updates
            if (clockRunnable == null) {
                clockRunnable = new Runnable() {
                    public void run() {
                        updateClock();
                        if (Services.tracks.logger.isLogging()) {
                            handler.postDelayed(this, clockUpdateInterval);
                        }
                    }
                };
                handler.post(clockRunnable);
            }
        } else {
            // Stop clock updates
            if (clockRunnable != null) {
                handler.removeCallbacks(clockRunnable);
                clockRunnable = null;
            }
        }
        updateClock();
        updateAudible();
        invalidateOptionsMenu();
    }

    public void clickAltimeter(View v) {
        firebaseAnalytics.logEvent("click_alti", null);
        startActivity(new Intent(this, AltimeterActivity.class));
    }

    public void clickNav(View v) {
        firebaseAnalytics.logEvent("click_nav", null);
        startActivity(new Intent(this, MapActivity.class));
    }

    public void clickAudible(View v) {
        if (Services.audible.settings.isEnabled) {
            // Stop audible
            firebaseAnalytics.logEvent("click_stop_audible", null);
            Services.audible.disableAudible();
        } else {
            // Start audible
            firebaseAnalytics.logEvent("click_start_audible", null);
            Services.audible.enableAudible(this);
        }
        updateUIState();
    }

    private final View.OnLongClickListener audibleLongClickListener = v -> {
        firebaseAnalytics.logEvent("long_click_audible", null);
        startActivity(new Intent(MainActivity.this, AudibleSettingsActivity.class));
        return true;
    };

    public void clickTracks(View v) {
        firebaseAnalytics.logEvent("click_tracks", null);
        startActivity(new Intent(this, TrackListActivity.class));
    }

    public void clickLasers(View v) {
        firebaseAnalytics.logEvent("click_lasers", null);
        startActivity(new Intent(this, LaserActivity.class));
    }

    public void clickFlysight(View v) {
        firebaseAnalytics.logEvent("click_flysight", null);
        startActivity(new Intent(this, FlysightActivity.class));
    }

    public void clickSettings(View v) {
        firebaseAnalytics.logEvent("click_settings", null);
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Update the text view for timer
     */
    private void updateClock() {
        if (Services.tracks.logger.isLogging()) {
            Services.tracks.logger.getLogTime(clockBuilder);
            binding.infoPanel.clock.setText(clockBuilder);
        } else {
            binding.infoPanel.clock.setText("");
        }
    }

    private final StringBuilder clockBuilder = new StringBuilder();

    /**
     * Update audible button state
     */
    private void updateAudible() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("audible_enabled", false)) {
            binding.audible.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.audio_on, 0, 0);
        } else {
            binding.audible.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.audio, 0, 0);
        }
    }

    // Listen for events
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoggingEvent(LoggingEvent event) {
        updateUIState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudibleEvent(AudibleEvent event) {
        updateUIState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (clockRunnable != null) {
            handler.removeCallbacks(clockRunnable);
            clockRunnable = null;
        }
        EventBus.getDefault().unregister(this);
    }

}
