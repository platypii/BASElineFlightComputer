package com.platypii.baseline.views.laser;

import android.widget.Toast;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.lasers.LaserDeleteTask;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserViewFragment extends Fragment implements DialogInterface.OnClickListener {
    private static final String TAG = "LaserView";

    static final String LASER_ID = "LASER_ID";

    private LaserProfile laser;

    @Nullable
    private AlertDialog deleteConfirmation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_view, container, false);
        EventBus.getDefault().register(this);

        try {
            // Get laser profile from fragment arguments
            laser = getLaser();
            render(view);
        } catch (IllegalStateException e) {
            Exceptions.report(e);
            getFragmentManager().popBackStack();
        }

        return view;
    }

    private void render(@NonNull View view) {
        // Bind delete button
        final View deleteButton = view.findViewById(R.id.laserDelete);
        deleteButton.setOnClickListener(this::delete);
        if (laser.user_id != null && laser.user_id.equals(AuthState.getUser())) {
            deleteButton.setVisibility(View.VISIBLE);
        }

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
    }

    private void delete(View view) {
        Log.i(TAG, "User clicked delete laser " + laser);
        final Bundle bundle = new Bundle();
        bundle.putString("laser_id", laser.laser_id);
        FirebaseAnalytics.getInstance(getContext()).logEvent("click_laser_delete_1", bundle);
        // Prompt user for confirmation
        deleteConfirmation = new AlertDialog.Builder(getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete this profile?")
                .setMessage(R.string.delete_remote)
                .setPositiveButton(R.string.action_delete, this)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * User clicked "ok" on delete laser
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Log.i(TAG, "User confirmed delete laser " + laser.laser_id);
            // Analytics
            final Bundle bundle = new Bundle();
            bundle.putString("laser_id", laser.laser_id);
            FirebaseAnalytics.getInstance(getContext()).logEvent("click_track_delete_remote_2", bundle);
            if (laser.isLocal()) {
                // Delete local only
                Services.cloud.lasers.unsynced.remove(laser);
                EventBus.getDefault().post(new LaserSyncEvent.DeleteSuccess(laser));
            } else {
                // Delete laser from server
                getView().findViewById(R.id.laserDelete).setEnabled(false);
                new Thread(new LaserDeleteTask(getContext(), laser)).start();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserDeleteSuccess(@NonNull LaserSyncEvent.DeleteSuccess event) {
        getFragmentManager().popBackStack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserDeleteFailure(@NonNull LaserSyncEvent.DeleteFailure event) {
        getView().findViewById(R.id.laserDelete).setEnabled(true);
        Toast.makeText(getContext(), "Failed to delete profile", Toast.LENGTH_SHORT).show();
    }

    /**
     * Get laser profile from fragment arguments
     */
    @NonNull
    private LaserProfile getLaser() throws IllegalStateException {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            final String laserId = bundle.getString(LASER_ID);
            if (laserId != null) {
                final LaserProfile laser = Services.cloud.lasers.get(laserId);
                if (laser != null) {
                    return laser;
                } else {
                    throw new IllegalStateException("Failed to load laser from arguments");
                }
            } else {
                throw new IllegalStateException("Failed to load laser id from arguments");
            }
        } else {
            throw new IllegalStateException("Failed to load arguments");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Dismiss alert to prevent context leak
        if (deleteConfirmation != null) {
            deleteConfirmation.dismiss();
            deleteConfirmation = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
