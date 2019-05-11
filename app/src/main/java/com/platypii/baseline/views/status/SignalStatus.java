package com.platypii.baseline.views.status;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.location.LocationStatus;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.Locale;

/**
 * Show the user their GPS status
 */
public class SignalStatus extends BaseStatus implements MyLocationListener {

    private TextView signalStatus;
    private TextView satelliteStatus;

    // Periodic UI updates
    private static final int signalUpdateInterval = 200; // milliseconds
    private final Handler handler = new Handler();
    private final Runnable signalRunnable = new Runnable() {
        public void run() {
            update();
            handler.postDelayed(this, signalUpdateInterval);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.status_panel, container, false);
        signalStatus = view.findViewById(R.id.signalStatus);
        satelliteStatus = view.findViewById(R.id.satelliteStatus);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initial update
        update();

        // Start signal updates
        handler.post(signalRunnable);

        // Listen for location updates
        Services.location.addListener(this);
    }


    /**
     * Update the views for GPS signal strength
     */
    private void update() {
        LocationStatus.updateStatus();
        signalStatus.setCompoundDrawablesWithIntrinsicBounds(LocationStatus.icon, 0, 0, 0);
        signalStatus.setText(LocationStatus.message);
        if (LocationStatus.satellites > 0) {
            satelliteStatus.setText(String.format(Locale.US, "%d", LocationStatus.satellites));
            satelliteStatus.setVisibility(View.VISIBLE);
        } else {
            satelliteStatus.setVisibility(View.GONE);
        }
    }

    private final Runnable updateRunnable = this::update;

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        handler.post(updateRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(signalRunnable);
        Services.location.removeListener(this);
    }

}
