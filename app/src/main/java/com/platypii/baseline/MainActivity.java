package com.platypii.baseline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.TheCloud;
import com.platypii.baseline.util.Callback;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private static final String TAG = "Main";

    private FirebaseAnalytics firebaseAnalytics;

    // app start time
    // public static final long startTimeMillis = System.currentTimeMillis();
    // public static final long startTimeNano = System.nanoTime();

    private Menu menu;

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

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
        if(!MyDatabase.isLogging()) {
            Log.i(TAG, "Starting logging");
            firebaseAnalytics.logEvent("click_logging_start", null);

            // Start logging
            MyDatabase.startLogging(getApplicationContext());
            updateUIState();
        } else {
            Log.i(TAG, "Stopping logging");
            firebaseAnalytics.logEvent("click_logging_stop", null);

            // Stop logging
            final Jump jump = MyDatabase.stopLogging();
            updateUIState();

            // Upload to the cloud
            if(jump != null) {
                uploadToCloud(jump);
            } else {
                Log.e(TAG, "Error reading log file");
            }
        }
    }

    private void uploadToCloud(final Jump jump) {
        if(isSignedIn()) {
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
                            Toast.makeText(MainActivity.this, "Track sync failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
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
        if(MyDatabase.isLogging()) {
            recordButton.setText(R.string.action_stop);
            recordButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.square, 0, 0);

            // Start clock updates
            if(clockRunnable == null) {
                clockRunnable = new Runnable() {
                    public void run() {
                        updateClock();
                        if (MyDatabase.isLogging()) {
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

    public void clickJumps(View v) {
        firebaseAnalytics.logEvent("click_tracks", null);
        startActivity(new Intent(this, JumpsActivity.class));
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
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            final SharedPreferences.Editor editor = prefs.edit();
            if (MyAudible.isEnabled()) {
                // Stop audible
                Toast.makeText(MainActivity.this, "Stopping audible", Toast.LENGTH_SHORT).show();
                editor.putBoolean("audible_enabled", false);
                editor.apply();

                MyAudible.stopAudible();
            } else {
                // Start audible
                Toast.makeText(MainActivity.this, "Starting audible", Toast.LENGTH_SHORT).show();
                editor.putBoolean("audible_enabled", true);
                editor.apply();

                MyAudible.startAudible();
            }
            updateUIState();
            return true;
        }
    };

    /**
     * Update the text view for timer
     */
    private void updateClock() {
        if(MyDatabase.isLogging()) {
            clock.setText(MyDatabase.getLogTime());
        } else {
            clock.setText("");
        }
    }

    /**
     * Update the views for GPS signal strength
     */
    private void updateSignal() {
        String status;
        int statusIcon;

        // GPS signal status
        if(BluetoothService.preferenceEnabled && BluetoothService.getState() != BluetoothService.BT_CONNECTED) {
            // Bluetooth enabled, but not connected
            statusIcon = R.drawable.warning;
            switch(BluetoothService.getState()) {
                case BluetoothService.BT_CONNECTING:
                    status = "GPS bluetooth connecting...";
                    break;
                case BluetoothService.BT_DISCONNECTED:
                    status = "GPS bluetooth not connected";
                    break;
                default:
                    status = "GPS bluetooth not connected";
                    Log.e(TAG, "Bluetooth inconsistent state: preference enabled, state = " + BluetoothService.getState());
            }
        } else {
            // Internal GPS, or bluetooth connected:
            if(Services.location.lastFixDuration() < 0) {
                // No fix yet
                status = "GPS searching...";
                statusIcon = R.drawable.status_red;
            } else {
                final long lastFixDuration = Services.location.lastFixDuration();
                // TODO: Use better method to determine signal.
                // Take into account acc and dop
                // How many of the last X expected fixes have we missed?
                if (lastFixDuration > 10000) {
                    status = String.format(Locale.getDefault(), "GPS last fix %ds", lastFixDuration / 1000L);
                    statusIcon = R.drawable.status_red;
                } else if (lastFixDuration > 2000) {
                    status = String.format(Locale.getDefault(), "GPS last fix %ds", lastFixDuration / 1000L);
                    statusIcon = R.drawable.status_yellow;
                } else if (BluetoothService.preferenceEnabled && BluetoothService.getState() == BluetoothService.BT_CONNECTED) {
                    status = String.format(Locale.getDefault(), "GPS bluetooth %.2fHz", Services.location.refreshRate);
                    statusIcon = R.drawable.status_blue;
                } else {
                    status = String.format(Locale.getDefault(), "GPS %.2fHz", Services.location.refreshRate);
                    statusIcon = R.drawable.status_green;
                }
            }
        }

        // Barometer status
        if(Double.isNaN(MyAltimeter.altitude)) {
            status += " (no barometer)";
        }

        signalStatus.setCompoundDrawablesWithIntrinsicBounds(statusIcon, 0, 0, 0);
        signalStatus.setText(status);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_sign_in:
                clickSignIn();
                return true;
            case R.id.menu_item_sign_out:
                clickSignOut();

                // Update menu
                updateMenu(false);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void handleSignInResult(GoogleSignInResult result) {
        super.handleSignInResult(result);
        updateMenu();
    }

    /**
     * Update menu based on signed in state
     */
    private void updateMenu() {
        updateMenu(isSignedIn());
    }
    /**
     * Update menu based on signed in state
     */
    private void updateMenu(boolean signedIn) {
        // Menu can be null if it hasn't been opened yet
        if(menu != null) {
            final MenuItem menuSignIn = menu.findItem(R.id.menu_item_sign_in);
            final MenuItem menuSignOut = menu.findItem(R.id.menu_item_sign_out);
            if(menuSignIn != null) {
                menuSignIn.setVisible(!signedIn);
            }
            if(menuSignOut != null) {
                menuSignOut.setVisible(signedIn);
            }
            invalidateOptionsMenu();
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
    }

}
