package com.platypii.baseline;

import com.platypii.baseline.altimeter.AnalogAltimeter;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends BaseActivity {
    private static final String TAG = "Altimeter";

    private AlertDialog alertDialog;

    private PolarPlot polar;
    private AnalogAltimeter analogAltimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);

        polar = (PolarPlot) findViewById(R.id.polar);
        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
        analogAltimeter.setOverlay(false);
        analogAltimeter.setLongClickable(true);
        analogAltimeter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                alertDialog = promptForAltitude(AltimeterActivity.this);
                return false;
            }
        });
    }

    private void updateFlightStats() {
        analogAltimeter.setAltitude(Services.alti.altitudeAGL());
    }

    public static AlertDialog promptForAltitude(final Activity activity) {
        Log.i(TAG, "Prompting for ground level adjustment");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.set_altitude_title);
        builder.setMessage(R.string.set_altitude_message);
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setHint("0");
        builder.setView(input);
        builder.setPositiveButton(R.string.set_altitude, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String inputText = input.getText().toString();
                final double units = Convert.metric? 1 : Convert.FT;
                final double altitude = inputText.isEmpty()? 0.0 : Numbers.parseDouble(inputText) * units;
                if(Numbers.isReal(altitude)) {
                    Log.w(TAG, "Setting altitude above ground level to " + altitude + "m");
                    Services.alti.setGroundLevel(Services.alti.pressure_altitude_filtered - altitude);
                } else {
                    Log.e(TAG, "Invalid altitude above ground level: " + altitude);
                    Toast.makeText(activity, "Invalid altitude", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog
        return builder.show();
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        updateFlightStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start sensor updates
        EventBus.getDefault().register(this);
        polar.start();
        updateFlightStats();
    }
    @Override
    public void onPause() {
        super.onPause();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
        polar.stop();
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}
