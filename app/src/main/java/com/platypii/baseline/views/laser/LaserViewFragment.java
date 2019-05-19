package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LaserViewFragment extends Fragment {
    static final String LASER_ID = "LASER_ID";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_view, container, false);

        render(view);

        return view;
    }

    private void render(@NonNull View view) {
        final LaserProfile laser = getLaser();
        if (laser != null) {
            // Name
            final TextView laserName = view.findViewById(R.id.laserName);
            laserName.setText(laser.name);
            // Location
            final TextView laserLocation = view.findViewById(R.id.laserLocation);
            final String location = laser.locationString();
            if (location.isEmpty()) {
                laserLocation.setVisibility(View.GONE);
            } else {
                laserLocation.setVisibility(View.VISIBLE);
                laserLocation.setText(location);
            }
            // Points label (show units)
            final TextView laserMeasurementsLabel = view.findViewById(R.id.laserMeasurementsLabel);
            final String units = Convert.metric ? "(m)" : "(ft)";
            laserMeasurementsLabel.setText("Points " + units);
            // Points
            final TextView laserText = view.findViewById(R.id.laserText);
            laserText.setText(LaserMeasurement.render(laser.points, Convert.metric));
        } else {
            Exceptions.report(new IllegalStateException("Failed to load laser"));
        }
    }

    /**
     * Get laser profile from fragment arguments
     */
    @Nullable
    private LaserProfile getLaser() {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            final String laserId = bundle.getString(LASER_ID);
            if (laserId != null) {
                return Services.cloud.lasers.cache.get(laserId);
            }
        }
        return null;
    }

}
