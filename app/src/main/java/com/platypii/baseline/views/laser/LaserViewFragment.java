package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.databinding.LaserViewBinding;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.lasers.LaserMeasurement;
import com.platypii.baseline.lasers.LaserProfile;
import com.platypii.baseline.lasers.cloud.LaserDeleteTask;
import com.platypii.baseline.util.ABundle;
import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
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
    private LaserViewBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LaserViewBinding.inflate(inflater, container, false);
        EventBus.getDefault().register(this);

        try {
            // Get laser profile from fragment arguments
            laser = getLaser();
            render();
        } catch (IllegalStateException e) {
            Exceptions.report(e);
            getParentFragmentManager().popBackStack();
        }

        return binding.getRoot();
    }

    private void render() {
        // Bind delete button
        binding.laserDelete.setOnClickListener(this::delete);
        if (laser.user_id != null && laser.user_id.equals(AuthState.getUser())) {
            binding.laserDelete.setVisibility(View.VISIBLE);
        }

        // Name
        binding.laserName.setText(laser.name);
        // Location
        final String location = laser.locationString();
        if (location.isEmpty()) {
            binding.laserLocation.setVisibility(View.GONE);
        } else {
            binding.laserLocation.setVisibility(View.VISIBLE);
            binding.laserLocation.setText(location);
        }
        // Points label (show units)
        final String units = Convert.metric ? "(m)" : "(ft)";
        binding.laserMeasurementsLabel.setText("Points " + units);
        // Points
        binding.laserText.setText(LaserMeasurement.render(laser.points, Convert.metric));
    }

    private void delete(View view) {
        Log.i(TAG, "User clicked delete laser " + laser);
        Analytics.logEvent(getContext(), "click_laser_delete_1", ABundle.of("laser_id", laser.laser_id));
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
            Analytics.logEvent(getContext(), "click_track_delete_remote_2", ABundle.of("laser_id", laser.laser_id));
            if (laser.isLocal()) {
                // Delete local only
                Services.lasers.unsynced.remove(laser);
                EventBus.getDefault().post(new LaserSyncEvent.DeleteSuccess(laser));
            } else {
                // Delete laser from server
                binding.laserDelete.setEnabled(false);
                new Thread(new LaserDeleteTask(laser)).start();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserDeleteSuccess(@NonNull LaserSyncEvent.DeleteSuccess event) {
        getParentFragmentManager().popBackStack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserDeleteFailure(@NonNull LaserSyncEvent.DeleteFailure event) {
        binding.laserDelete.setEnabled(true);
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
                final LaserProfile laser = Services.lasers.get(laserId);
                if (laser != null) {
                    return laser;
                } else {
                    throw new IllegalStateException("Failed to load laser " + laserId + " from arguments");
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
