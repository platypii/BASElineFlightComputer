package com.platypii.baseline;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.MyFlightManager;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.measurements.MLocation;

/**
 * A self-updating TextView to display the altitude
 */
public class AltitudeTextView extends TextView implements MyAltitudeListener {

    public AltitudeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Start altitude updates
        MyAltimeter.addListener(this);
    }

    // Altitude updates
    @Override
    public void altitudeDoInBackground(MAltitude alt) {}
    @Override
    public void altitudeOnPostExecute() {
        final MLocation loc = MyLocationManager.lastLoc;
        final String altitude = " • " + Convert.distance(MyAltimeter.altitudeAGL());
        final String fallrate = (MyAltimeter.climb < 0)? " ↓ " + Convert.speed(-MyAltimeter.climb) : " ↑ " + Convert.speed(MyAltimeter.climb);
        final String groundSpeed = "→ " + Convert.speed(loc.groundSpeed());
        final String glideRatio = "◢ " + Convert.glide(loc.glideRatio());

        setText(altitude + "\n" + fallrate + "\n" + groundSpeed + "\n" + glideRatio);
    }

}