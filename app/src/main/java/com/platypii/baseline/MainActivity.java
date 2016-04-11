package com.platypii.baseline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
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
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.TheCloud;
import com.platypii.baseline.util.Callback;

public class MainActivity extends BaseActivity {
    private static final String TAG = "Main";

    // app start time
    // public static final long startTimeMillis = System.currentTimeMillis();
    public static final long startTimeNano = System.nanoTime();

    private Menu menu;

    private Button recordButton;
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
        clock = (TextView) findViewById(R.id.clock);
        signalStatus = (TextView) findViewById(R.id.signalStatus);

        final Button audibleButton = (Button) findViewById(R.id.audibleButton);
        audibleButton.setOnLongClickListener(new View.OnLongClickListener() {
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
                return false;
            }
        });
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
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()  // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    public void clickRecord(View v) {
        if(!MyDatabase.isLogging()) {
            Log.i(TAG, "Starting logging");

            // Start logging
            MyDatabase.startLogging(getApplicationContext());
            updateUIState();
        } else {
            Log.i(TAG, "Stopping logging");

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
        invalidateOptionsMenu();
    }

    public void clickNav(View v) {
        // Open nav activity
        startActivity(new Intent(this, MapActivity.class));
    }

    public void clickJumps(View v) {
        // Open jumps activity
        final Intent intent = new Intent(this, JumpsActivity.class);
        startActivity(intent);
    }

    public void clickAudible(View v) {
        // Open audible activity
        final Intent intent = new Intent(this, AudibleSettingsActivity.class);
        startActivity(intent);
    }

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
        if(Services.location.lastFixDuration() < 0) {
            status = "no signal";
            statusIcon = R.drawable.status_red;
        } else {
            final long lastFixDuration = Services.location.lastFixDuration();
            if(lastFixDuration > 10000) {
                status = "no signal";
                statusIcon = R.drawable.status_red;
            } else if(lastFixDuration > 3000) {
                status = "weak signal";
                statusIcon = R.drawable.status_yellow;
            } else {
                status = "good signal";
                statusIcon = R.drawable.status_green;
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
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Only enable the menu if we are NOT logging
        return !MyDatabase.isLogging();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_settings:
                // Open settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_item_sensor_info:
                // Open sensor activity
                startActivity(new Intent(this, SensorActivity.class));
                return true;
            case R.id.menu_item_sign_in:
                clickSignIn();
                return true;
            case R.id.menu_item_sign_out:
                clickSignOut();

                // Update menu
                if(menu != null) {
                    menu.findItem(R.id.menu_item_sign_in).setVisible(true);
                    menu.findItem(R.id.menu_item_sign_out).setVisible(false);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void handleSignInResult(GoogleSignInResult result) {
        super.handleSignInResult(result);
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        if(result.isSuccess()) {
            // Update menu
            if(menu != null) {
                menu.findItem(R.id.menu_item_sign_in).setVisible(false);
                menu.findItem(R.id.menu_item_sign_out).setVisible(true);
            }
        } else {
            // Update menu
            if(menu != null) {
                menu.findItem(R.id.menu_item_sign_in).setVisible(true);
                menu.findItem(R.id.menu_item_sign_out).setVisible(false);
            }
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
