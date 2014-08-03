package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.ui.GroundLevelWidget.GroundLevelWidgetListener;
import com.platypii.baseline.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Activity that allows the user to set the ground level
 * @author platypii
 */
public class GroundLevelActivity extends Activity implements MyAltitudeListener, GroundLevelWidgetListener {

    // Views
    private GroundLevelWidget widget;
    private TextView altitudeLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.groundlevel);

        // Find views
        altitudeLabel = (TextView)findViewById(R.id.altitudeLabel);
        widget = (GroundLevelWidget)findViewById(R.id.groundLevelWidget);

        // Action bar
        getActionBar().hide();

        // Load saved offset
        if(savedInstanceState != null && savedInstanceState.containsKey("Offset")) {
            widget.setOffset(savedInstanceState.getDouble("Offset"));
        }

        // Notify us when the user changes the altitude offset, or the pressure changes
        widget.addListener(this);
        MyAltimeter.addListener(this);

    }

    private void update() {
        // Update altitude and pressure
        double altitude = MyAltimeter.altitude + widget.offset;
        if(Double.isNaN(altitude)) altitude = widget.offset; // TODO: Pre-set the offset?
        altitudeLabel.setText(Convert.distance(altitude));
        widget.invalidate(); // redraw
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save tab state
        outState.putDouble("Offset", widget.offset);
    }

    public void clickZero(View v) {
        // Adjust offset
        double altitude = Double.isNaN(MyAltimeter.altitude)? 0 : MyAltimeter.altitude;
        widget.setOffset(-altitude);
        update();
    }

    public void clickSet(View v) {
        // Adjust barometer
        MyAltimeter.setGroundLevel(MyAltimeter.ground_level - widget.offset);
        widget.setOffset(0);
        finish();
    }

    public void clickCancel(View v) {
        finish();
    }

    // Listeners
    public void altitudeDoInBackground(MyAltitude alt) {}
    public void altitudeOnPostExecute() {
        update();
    }
    public void onGroundLevelChanged() {
        update();
    }

}
