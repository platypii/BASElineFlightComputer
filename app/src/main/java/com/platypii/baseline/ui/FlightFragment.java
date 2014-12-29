package com.platypii.baseline.ui;

import com.platypii.baseline.audible.MyFlightManager;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.R;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class FlightFragment extends Fragment implements MyLocationListener {

    private TextView etgLabel;
    private TextView distLabel;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private static final int updateInterval = 80; // in milliseconds


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.flight, container, false);

        // Find UI elements
        etgLabel = (TextView)view.findViewById(R.id.etgLabel);
        distLabel = (TextView)view.findViewById(R.id.distLabel);

        // Initial update
        update();

        // GPS updates
        MyLocationManager.addListener(this);

        // Periodic UI updates
        handler.post(new Runnable() {
            public void run() {
                AltimeterFragment.updateAlti(view);
                update();
                handler.postDelayed(this, updateInterval);
            }
        });

        return view;
    }

    private void updateGPS(MyLocation loc) {
        if(loc != null && MyFlightManager.homeLoc != null) {
            double distance = loc.loc().distanceTo(MyFlightManager.homeLoc);
            distLabel.setText(Convert.distance2(distance));
        } else {
            distLabel.setText("");
        }
    }

    // Updates the UI elements
    private void update() {
        double etg = -MyAltimeter.altitude / MyAltimeter.climb;
        if(Double.isNaN(etg) || Double.isInfinite(etg) || etg < 0.01 || Math.abs(MyAltimeter.climb) < 0.1 * Convert.MPH || 24 * 60 * 60 < etg)
            etgLabel.setText("");
        else
            etgLabel.setText(Convert.time3((long) (1000 * etg)));
    }

    // Location updates
    public void onLocationChanged(final MyLocation loc) {
        handler.post(new Runnable() {
            public void run() {
                updateGPS(loc);
            }
        });
    }

}
