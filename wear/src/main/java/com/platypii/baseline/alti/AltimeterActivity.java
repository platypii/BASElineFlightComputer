package com.platypii.baseline.alti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.platypii.baseline.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends FragmentActivity {
    private static final String TAG = "Altimeter";

    private final MyAltimeter alti = new MyAltimeter();
    private AnalogAltimeter analogAltimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
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
        analogAltimeter.setAltitude(alti.altitudeAGL());
    }

    public void promptForAltitude(final Activity activity) {
        Log.i(TAG, "Prompting for ground level adjustment");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Set Altitude AGL");
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
                    alti.setGroundLevel(alti.pressure_altitude_filtered - altitude);
                } else {
                    Log.e(TAG, "Invalid altitude above ground level: " + altitude);
                    Toast.makeText(activity, "Invalid altitude", Toast.LENGTH_SHORT).show();
                }
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
        updateFlightStats();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start altimeter
        alti.start(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start sensor updates
        EventBus.getDefault().register(this);
        updateFlightStats();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop altimeter
        alti.stop();
    }
}
