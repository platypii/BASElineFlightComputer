package com.platypii.baseline.views;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.location.LocationStatus;
import com.platypii.baseline.views.altimeter.AltimeterActivity;
import com.platypii.baseline.views.map.MapActivity;
import com.platypii.baseline.views.tracks.TrackListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity {
    private static final String TAG = "Main";

    // app start time
    // public static final long startTimeMillis = System.currentTimeMillis();
    // public static final long startTimeNano = System.nanoTime();

    private Button recordButton;
    private Button audibleButton;
    private TextView clock;
    private TextView signalStatus;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int clockUpdateInterval = 32; // milliseconds
    private final int signalUpdateInterval = 200; // milliseconds

    private Runnable clockRunnable;
    private Runnable signalRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // enableStrictMode();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        recordButton = findViewById(R.id.recordButton);
        audibleButton = findViewById(R.id.audibleButton);
        clock = findViewById(R.id.clock);
        signalStatus = findViewById(R.id.signalStatus);

        if(audibleButton != null) {
            audibleButton.setOnLongClickListener(audibleLongClickListener);
        }

        // Handle intents
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(@NonNull Intent intent) {
        final String intentType = intent.getType();
        if("baseline/stop".equals(intentType)) {
            // Stop audible and logging
            Services.logger.stopLogging();
            Services.audible.disableAudible();
        } else if(intentType != null) {
            Log.w(TAG, "Unknown intent type: " + intentType);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Restore start/stop state
        updateUIState();

        // Start signal updates
        if(signalRunnable == null) {
            signalRunnable = new Runnable() {
                public void run() {
                    updateSignal();
                    handler.postDelayed(this, signalUpdateInterval);
                }
            };
            handler.post(signalRunnable);
        } else {
            Log.e(TAG, "Signal updates already started");
        }

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
        if(Services.location.lastLoc != null) {
            bundle.putFloat("lat", (float) Services.location.lastLoc.latitude);
            bundle.putFloat("lon", (float) Services.location.lastLoc.longitude);
        }
        if(!Services.logger.isLogging()) {
            firebaseAnalytics.logEvent("click_logging_start", bundle);
            Services.logger.startLogging();
        } else {
            firebaseAnalytics.logEvent("click_logging_stop", bundle);
            Services.logger.stopLogging();
        }
    }

    // Enables buttons and clock
    private void updateUIState() {
        if(Services.logger.isLogging()) {
            recordButton.setText(R.string.action_stop);
            recordButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.square, 0, 0);

            // Start clock updates
            if(clockRunnable == null) {
                clockRunnable = new Runnable() {
                    public void run() {
                        updateClock();
                        if (Services.logger.isLogging()) {
                            handler.postDelayed(this, clockUpdateInterval);
                        }
                    }
                };
                handler.post(clockRunnable);
            }
        } else {
            recordButton.setText(R.string.action_record);
            recordButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.circle, 0, 0);
            clock.setText("");

            // Stop clock updates
            if(clockRunnable != null) {
                handler.removeCallbacks(clockRunnable);
                clockRunnable = null;
            }
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("audible_enabled", false)) {
            audibleButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.audio_on, 0, 0);
        } else {
            audibleButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.audio, 0, 0);
        }
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
        if (Services.audible.isEnabled()) {
            // Stop audible
            firebaseAnalytics.logEvent("click_stop_audible", null);
            Services.audible.disableAudible();
        } else {
            // Start audible
            firebaseAnalytics.logEvent("click_start_audible", null);
            Toast.makeText(MainActivity.this, "Starting audible", Toast.LENGTH_SHORT).show();
            Services.audible.enableAudible();
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

    public void clickSettings(View v) {
        firebaseAnalytics.logEvent("click_settings", null);
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Update the text view for timer
     */
    private void updateClock() {
        if(Services.logger.isLogging()) {
            clock.setText(Services.logger.getLogTime());
        } else {
            clock.setText("");
        }
    }

    /**
     * Update the views for GPS signal strength
     */
    private void updateSignal() {
        final LocationStatus status = LocationStatus.getStatus();
        signalStatus.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0);
        signalStatus.setText(status.message);
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncEvent(SyncEvent event) {
        if(event instanceof SyncEvent.UploadSuccess) {
            Toast.makeText(MainActivity.this, "Track sync success", Toast.LENGTH_SHORT).show();
        } else if(event instanceof SyncEvent.UploadFailure) {
            final SyncEvent.UploadFailure uploadFailure = (SyncEvent.UploadFailure) event;
            Toast.makeText(MainActivity.this, "Track sync failed: " + uploadFailure.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(clockRunnable != null) {
            handler.removeCallbacks(clockRunnable);
            clockRunnable = null;
        }
        if(signalRunnable != null) {
            handler.removeCallbacks(signalRunnable);
            signalRunnable = null;
        }
        EventBus.getDefault().unregister(this);
    }

}
