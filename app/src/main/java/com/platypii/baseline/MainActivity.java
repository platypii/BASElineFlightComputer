package com.platypii.baseline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.location.LocationStatus;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.cloud.TheCloud;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.util.Callback;
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
        recordButton = (Button) findViewById(R.id.recordButton);
        audibleButton = (Button) findViewById(R.id.audibleButton);
        clock = (TextView) findViewById(R.id.clock);
        signalStatus = (TextView) findViewById(R.id.signalStatus);

        if(audibleButton != null) {
            audibleButton.setOnLongClickListener(audibleLongClickListener);
        }
    }

    @Override
    public void onStart() {
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
        if(!Services.logger.isLogging()) {
            firebaseAnalytics.logEvent("click_logging_start", null);
            Services.logger.startLogging(getApplicationContext());
        } else {
            firebaseAnalytics.logEvent("click_logging_stop", null);
            final TrackFile trackList = Services.logger.stopLogging();

            // Upload to the cloud
            if(trackList != null) {
                uploadToCloud(trackList);
            } else {
                Log.e(TAG, "Error reading track file");
            }
        }
    }

    private void uploadToCloud(final TrackFile jump) {
        if(isSignedIn()) {
            // Begin automatic upload
            getAuthToken(new Callback<String>() {
                @Override
                public void apply(String authToken) {
                    TheCloud.upload(jump, authToken, null);
                }

                @Override
                public void error(String error) {
                    Toast.makeText(MainActivity.this, "Track sync failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w(TAG, "Track sync failed: not signed in");
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

    public void clickTracks(View v) {
        firebaseAnalytics.logEvent("click_tracks", null);
        startActivity(new Intent(this, TrackListActivity.class));
    }

    public void clickAudible(View v) {
        firebaseAnalytics.logEvent("click_audible", null);
        startActivity(new Intent(this, AudibleSettingsActivity.class));
    }

    public void clickSettings(View v) {
        firebaseAnalytics.logEvent("click_settings", null);
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private final View.OnLongClickListener audibleLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
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
            return true;
        }
    };

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
        if(event.type == SyncEvent.SYNC_UPLOAD_SUCCESS) {
            Toast.makeText(MainActivity.this, "Track sync success", Toast.LENGTH_SHORT).show();
        } else if(event.type == SyncEvent.SYNC_UPLOAD_FAILED) {
            Toast.makeText(MainActivity.this, "Track sync failed: " + event.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
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
