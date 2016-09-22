package com.platypii.baseline;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Util;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends Activity {
    private static final String TAG = "Altimeter";

    private AnalogAltimeter analogAltimeter;
    private TextView flightStatsVario;
    private TextView flightStatsSpeed;
    private TextView flightStatsGlide;

    // Activity state
    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);

        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
        flightStatsVario = (TextView) findViewById(R.id.flightStatsVario);
        flightStatsSpeed = (TextView) findViewById(R.id.flightStatsSpeed);
        flightStatsGlide = (TextView) findViewById(R.id.flightStatsGlide);

        analogAltimeter.setOverlay(false);
        analogAltimeter.setLongClickable(true);
        analogAltimeter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                promptForAltitude(AltimeterActivity.this);
                return false;
            }
        });
    }

    private void updateFlightStats() {
        analogAltimeter.setAltitude(MyAltimeter.altitudeAGL());
        final MLocation loc = Services.location.lastLoc;
        if(MyAltimeter.climb < 0) {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_downward_white_24dp,0,0,0);
            flightStatsVario.setText(Convert.speed(-MyAltimeter.climb));
        } else {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_upward_white_24dp,0,0,0);
            flightStatsVario.setText(Convert.speed(MyAltimeter.climb));
        }
        if(loc != null) {
            flightStatsSpeed.setText(Convert.speed(loc.groundSpeed()));
            flightStatsGlide.setText(loc.glideRatioString());
        }
    }

    public static void promptForAltitude(final Activity activity) {
        Log.i(TAG, "Prompting for ground level adjustment");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Set Altitude AGL");
        builder.setMessage("Altitude above ground level in feet");
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setHint("0");
        builder.setView(input);
        builder.setPositiveButton(R.string.set_altitude, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String inputText = input.getText().toString();
                final double altitude = inputText.isEmpty()? 0.0 : Util.parseDouble(inputText) * Convert.FT;
                if(Util.isReal(altitude)) {
                    Log.w(TAG, "Setting altitude above ground level to " + altitude + "m");
                    MyAltimeter.setGroundLevel(MyAltimeter.pressure_altitude_filtered - altitude);
                } else {
                    Log.e(TAG, "Invalid altitude above ground level: " + altitude);
                    Toast.makeText(activity, "Invalid altitude", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        if(!paused) {
            updateFlightStats();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        updateFlightStats();
    }
    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
        // Start sensor updates
        EventBus.getDefault().register(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
        // Stop flight services
        Services.stop();
    }
}
