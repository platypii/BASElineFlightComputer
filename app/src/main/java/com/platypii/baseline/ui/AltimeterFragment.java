package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.R;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AltimeterFragment extends Fragment {

    // Periodic UI updates
    private final Handler handler = new Handler();
    private static final int updateInterval = 80; // in milliseconds

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.altimeter, container, false);

        // Initial update
        updateAlti(view);

        // Periodic UI updates
        handler.post(new Runnable() {
            public void run() {
                AltimeterFragment.updateAlti(view);
                handler.postDelayed(this, updateInterval);
            }
        });

        return view;
    }

    /**
     * Updates the altitude panel
     */
    public static void updateAlti(View view) {
        // Find UI elements:
        TextView altitudeLabel = (TextView)view.findViewById(R.id.altitudeLabel);
        TextView climbLabel = (TextView)view.findViewById(R.id.climbLabel);
        // Update views
        altitudeLabel.setText(Convert.distance(MyAltimeter.altitude));
        climbLabel.setText(Convert.speed2(MyAltimeter.climb));
    }

}
