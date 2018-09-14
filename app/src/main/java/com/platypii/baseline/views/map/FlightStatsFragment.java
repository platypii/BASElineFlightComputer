package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FlightStatsFragment extends Fragment implements MyLocationListener {

    private TextView flightStatsVario;
    private TextView flightStatsSpeed;
    private TextView flightStatsGlide;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.flight_stats, container, false);
        flightStatsVario = view.findViewById(R.id.flightStatsVario);
        flightStatsSpeed = view.findViewById(R.id.flightStatsSpeed);
        flightStatsGlide = view.findViewById(R.id.flightStatsGlide);
        return view;
    }

    void update() {
        if (Services.alti.climb < 0) {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_downward_white_24dp, 0, 0, 0);
            flightStatsVario.setText(Convert.speed(-Services.alti.climb));
        } else {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_upward_white_24dp, 0, 0, 0);
            flightStatsVario.setText(Convert.speed(Services.alti.climb));
        }
        final double groundSpeed = Services.location.groundSpeed();
        if (Numbers.isReal(groundSpeed)) {
            flightStatsSpeed.setText(Convert.speed(groundSpeed));
            flightStatsGlide.setText(Convert.glide(groundSpeed, Services.alti.climb, 2, true));
        } else {
            flightStatsSpeed.setText("");
            flightStatsGlide.setText("");
        }
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        update();
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        getActivity().runOnUiThread(this::update);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start sensor updates
        Services.location.addListener(this);
        EventBus.getDefault().register(this);
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop sensor updates
        Services.location.removeListener(this);
        EventBus.getDefault().unregister(this);
    }

}