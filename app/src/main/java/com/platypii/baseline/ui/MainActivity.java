package com.platypii.baseline.ui;

import com.platypii.baseline.audible.EventsActivity;
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.audible.MyFlightManager;
import com.platypii.baseline.audible.MySoundManager;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.MySensorManager;
import com.platypii.baseline.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;


// TODO: Swipe between activities
// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE); // set type of animation
// ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

public class MainActivity extends Activity {
    
	private Context appContext;
    // private WakeLock wakeLock;

    public static final long startTime = System.currentTimeMillis(); // Session start time (when the app started)

    // Method tracing. This is slow! Exports log when back is pressed.
    private static final boolean PROFILING = false;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private static final int updateInterval = 500; // in milliseconds

    // Menu items
	private MenuItem gpsIcon;
	private MenuItem alertIcon;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	if(PROFILING) {
    		Debug.startMethodTracing(); // Method tracing is SLOW
            Log.w("Profiling", "Method tracing started");
    	}

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        // To fix color banding:
        getWindow().setFormat(PixelFormat.RGBA_8888);
        //getWindow().addFlags(LayoutParams.FLAG_DITHER);
        setContentView(R.layout.main);
        appContext = getApplication(); // Application context (activity context is fragile)
        
        // Hide soft keys
        // TODO: if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        final View rootView = findViewById(R.id.tab_container);
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        rootView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                // wait, then hide
                handler.postDelayed(new Runnable() {
                    public void run() {
                        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }, 1000);
            }
        });
        
        // Acquire a wakelock to force the screen and power to stay on (this is very aggressive, and sucks power!)
        // PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        // wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My wakelook");
        // wakeLock.acquire();

        // Initialize Services
        Log.i("Main", "Initializing sound");
        MySoundManager.initSounds(appContext);
        Log.i("Main", "Initializing database");
        MyDatabase.initDatabase(appContext);
        Log.i("Main", "Initializing location");
        MyLocationManager.initLocation(appContext);
        Log.i("Main", "Initializing sensors");
        MySensorManager.initSensors(appContext);
        Log.i("Main", "Initializing altimeter");
        MyAltimeter.initAltimeter(appContext);
        Log.i("Main", "Initializing flight");
        MyFlightManager.initFlight(appContext);
        Log.i("Main", "Initializing audible");
        MyAudible.initAudible();
        
        // Check for barometer, gps enabled, etc
        systemCheck();
        
        // Action bar
        final ActionBar bar = getActionBar();
        if(bar != null) {
            Log.i("Main", "Initializing action bar");
            bar.setDisplayShowTitleEnabled(false);
            bar.setDisplayShowHomeEnabled(false);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Tabs
            bar.addTab(bar.newTab().setText("Alti")
                    .setTabListener(new TabListener<>(this, "Altimeter", AltimeterFragment.class)), true);
            bar.addTab(bar.newTab().setText("Flight")
                    .setTabListener(new TabListener<>(this, "Flight", FlightFragment.class)));
            bar.addTab(bar.newTab().setText("Data")
                    .setTabListener(new TabListener<>(this, "Data", DataFragment.class)));
            bar.addTab(bar.newTab().setText("Map")
                    .setTabListener(new TabListener<>(this, "Map", AccuracyFragment.class)));
            if (savedInstanceState != null && savedInstanceState.containsKey("Tab"))
                bar.setSelectedNavigationItem(savedInstanceState.getInt("Tab"));
            bar.show();
        } else {
            Log.e("Main", "No action bar!");
        }
    }

	/**
	 * Perform startup checks, and if it fails, notify the user
	 */
    void systemCheck() {
		// Check for GPS enabled
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// Show alert dialog
	    	new AlertDialog.Builder(this)
	    	.setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("GPS Error!")
	        .setMessage("Please enable GPS for full functionality.")
	        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int which) {
	            	// Open location settings
	            	startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	            }
	        })
	        .setNegativeButton("Cancel", null)
	        .show();
		}

		// Check for Altimeter
		if(!MySensorManager.hasSensor(Sensor.TYPE_PRESSURE)) {
			// Show alert dialog
	    	new AlertDialog.Builder(this)
	    	.setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("Altimeter Error!")
	        .setMessage("This device does not appear to have a barometric altimeter. Using GPS for altitude data is NOT RELIABLE.\nProceed with caution. ")
	        .setPositiveButton("OK", null)
	        //.setNegativeButton("Cancel", null)
	        .show();
		}
	}
	
    // Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        updateActionBar();
        
        // Find menu items
        gpsIcon = menu.findItem(R.id.gpsOption);
        alertIcon = menu.findItem(R.id.alertOption);

        // Periodic Action Bar updates
        handler.post(new Runnable() {
            public void run() {
                updateActionBar();
                handler.postDelayed(this, updateInterval);
            }
        });

        return true;
    }
	/**
	 * Updates the action bar icons
	 */
	private void updateActionBar() {
		// Update GPS Icon
		if(gpsIcon != null) {
            long timeSinceLastFix = System.currentTimeMillis() - MyLocationManager.lastFixMillis;
            if(timeSinceLastFix > 3000) {
    	        gpsIcon.setIcon(R.drawable.ic_menu_gps_searching);
            } else {
            	gpsIcon.setIcon(R.drawable.ic_menu_gps_found);
            }
		}

		// Alert icon
		if(alertIcon != null) {
			final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
			boolean gps_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean barometer_enabled = MySensorManager.hasSensor(Sensor.TYPE_PRESSURE);
			if(gps_enabled && barometer_enabled) {
				alertIcon.setVisible(false);
			} else {
				alertIcon.setVisible(true);
			}
		}
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.jumpsOption:
                // Open sensors activity
                startActivity(new Intent(this, JumpsActivity.class));
                break;
            case R.id.audibleOption:
                // Open settings activity
                startActivity(new Intent(this, EventsActivity.class));
                break;
            case R.id.settingsOption:
                // Open settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.gpsOption:
            	// Popup dialog for GPS options
            	gpsOptions();
                break;
            case R.id.alertOption:
                // Run a system check and notify user of problems
            	systemCheck();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
    	// Stop method tracing and export log file
    	if(PROFILING) {
    		Debug.stopMethodTracing();
	        Log.e("Profiling", "Method tracing stopped");
    	}

    	// Check mode, and ignore if in flight mode
    	// TODO: Popup anyway if pressed 3x
    	if(MyFlightManager.flightMode == null || MyFlightManager.flightMode.equals("") || MyFlightManager.flightMode.equals("Ground")) {
	    	// Confirm exit with user (like google nav)
	    	new AlertDialog.Builder(this)
	    	.setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("Exit flight computer?")
	        .setMessage("This will end logging and audible feedback.")
	        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	// Terminate the app
	            	terminate();
	            }
	        })
	        .setNegativeButton("Cancel", null)
	        .show();
    	}
    }
    
    // TODO: check if any events require GPS
    // Show a popup dialog to set GPS options
    private void gpsOptions() {
    	new AlertDialog.Builder(this)
    	.setTitle("GPS is " + (MyLocationManager.isEnabled? "enabled" : "disabled"))
    	.setSingleChoiceItems(new String[] {"Enable GPS", "Disable GPS"},
							  (MyLocationManager.isEnabled? 0 : 1),
							  new OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									MyLocationManager.setEnabled(which == 0);
									dialog.dismiss();
								}
    						  })
    	.show();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// Save tab state
        final ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            outState.putInt("Tab", actionBar.getSelectedNavigationIndex());
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Main", "Destroying!");
        // Terminate jump
        if(MyFlightManager.jumping) {
            MyDatabase.jumps.endJump(System.currentTimeMillis());
        }
        
        // TODO: Flush cache

        // Release wakelock
        // wakeLock.release();
    }
    
    private void terminate() {
        Log.w("Main", "terminating application");
        if(MyFlightManager.jumping) {
            MyDatabase.jumps.endJump(System.currentTimeMillis());
        }
        MainActivity.this.finish();
        System.exit(0);
    }
    
}



