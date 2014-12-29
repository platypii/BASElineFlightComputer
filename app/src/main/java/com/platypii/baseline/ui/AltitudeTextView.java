package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * A self-updating TextView to display the altitude
 * @author platypii
 *
 */
public class AltitudeTextView extends TextView implements MyAltitudeListener {

    public AltitudeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Start altitude updates
        MyAltimeter.addListener(this);
        
    }

    // Altitude updates
    public void altitudeDoInBackground(MyAltitude alt) {}
    public void altitudeOnPostExecute() {
        AltitudeTextView.this.setText(Convert.distance(MyAltimeter.altitude));
    }

}