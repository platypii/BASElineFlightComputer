package com.platypii.baseline.views.charts;

import com.platypii.baseline.databinding.ChartStatsBinding;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackStats;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.tracks.TrackDataActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ChartStatsFragment extends Fragment {

    @Nullable
    private TrackStats stats;

    private ChartStatsBinding binding;

    @NonNull
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ChartStatsBinding.inflate(inflater, container, false);

        // Get track data from parent activity
        final Handler handler = new Handler();
        final Activity parent = getActivity();
        if (parent instanceof TrackDataActivity) {
            ((TrackDataActivity) parent).trackData.thenAccept(trackData -> {
                this.stats = trackData.stats;
                // Notify self to update (on main thread) now that data is ready
                handler.post(() -> onUnFocus(null));
            });
        }

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Update views for focus event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackFocus(@NonNull ChartFocusEvent.TrackFocused event) {
        final MLocation focus = event.location;
        // TODO: Date should have timezone
        binding.timeLabel.setText(timeFormat.format(new Date(focus.millis)));
        binding.altitudeLabel.setText(Convert.distance(focus.altitude_gps) + " MSL");
        binding.speedLabel.setText(Convert.speed(focus.totalSpeed()));
        binding.glideLabel.setText(Convert.glide(focus.groundSpeed(), focus.climb, 1, true));
        if (stats != null && stats.exit != null) {
            binding.horizontalDistLabel.setText(Convert.distance(focus.distanceTo(stats.exit)));
            final double vdist = focus.altitude_gps - stats.exit.altitude_gps;
            if (vdist < 0) {
                binding.verticalDistLabel.setText("↓ " + Convert.distance(-vdist));
            } else {
                binding.verticalDistLabel.setText("↑ " + Convert.distance(vdist));
            }
        } else {
            binding.horizontalDistLabel.setText("");
            binding.verticalDistLabel.setText("");
        }
        binding.horizontalSpeedLabel.setText(Convert.speed(focus.groundSpeed()));
        if (focus.climb < 0) {
            binding.verticalSpeedLabel.setText("↓ " + Convert.speed(-focus.climb));
        } else {
            binding.verticalSpeedLabel.setText("↑ " + Convert.speed(focus.climb));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnFocus(@Nullable ChartFocusEvent.Unfocused event) {
        if (stats != null) {
            if (stats.exit != null) {
                binding.timeLabel.setText(timeFormat.format(new Date(stats.exit.millis)));
            } else {
                binding.timeLabel.setText("");
            }
            if (!stats.altitude.isEmpty()) {
                binding.altitudeLabel.setText(Convert.distance(stats.altitude.max - stats.altitude.min));
            } else {
                binding.altitudeLabel.setText("");
            }
            if (stats.exit != null) {
                binding.horizontalDistLabel.setText(Convert.distance(stats.exit.distanceTo(stats.land)));
                binding.verticalDistLabel.setText(Convert.distance(stats.altitude.range()));
            } else {
                binding.horizontalDistLabel.setText("");
                binding.verticalDistLabel.setText("");
            }
        } else {
            binding.timeLabel.setText("");
            binding.altitudeLabel.setText("");
            binding.horizontalDistLabel.setText("");
            binding.verticalDistLabel.setText("");
        }
        binding.horizontalSpeedLabel.setText("");
        binding.verticalSpeedLabel.setText("");
        binding.speedLabel.setText("");
        binding.glideLabel.setText("");
    }
}
