package com.platypii.baseline.views.charts;

import com.platypii.baseline.R;
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
import android.widget.TextView;
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

    private TextView timeLabel;
    private TextView altitudeLabel;
    private TextView horizontalDistLabel;
    private TextView verticalDistLabel;
    private TextView horizontalSpeedLabel;
    private TextView verticalSpeedLabel;
    private TextView speedLabel;
    private TextView glideLabel;

    @NonNull
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chart_stats, container, false);
        timeLabel = view.findViewById(R.id.timeLabel);
        altitudeLabel = view.findViewById(R.id.altitudeLabel);
        horizontalDistLabel = view.findViewById(R.id.hDistLabel);
        verticalDistLabel = view.findViewById(R.id.vDistLabel);
        horizontalSpeedLabel = view.findViewById(R.id.hSpeedLabel);
        verticalSpeedLabel = view.findViewById(R.id.vSpeedLabel);
        speedLabel = view.findViewById(R.id.speedLabel);
        glideLabel = view.findViewById(R.id.glideLabel);

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

        return view;
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
        timeLabel.setText(timeFormat.format(new Date(focus.millis)));
        altitudeLabel.setText(Convert.distance(focus.altitude_gps) + " MSL");
        speedLabel.setText(Convert.speed(focus.totalSpeed()));
        glideLabel.setText(Convert.glide(focus.groundSpeed(), focus.climb, 1, true));
        if (stats != null && stats.exit != null) {
            horizontalDistLabel.setText(Convert.distance(focus.distanceTo(stats.exit)));
            verticalDistLabel.setText(Convert.distance(focus.altitude_gps - stats.exit.altitude_gps));
        } else {
            horizontalDistLabel.setText("");
            verticalDistLabel.setText("");
        }
        horizontalSpeedLabel.setText(Convert.speed(focus.groundSpeed()));
        verticalSpeedLabel.setText(Convert.speed(focus.climb));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnFocus(@Nullable ChartFocusEvent.Unfocused event) {
        if (stats != null) {
            if (stats.exit != null) {
                timeLabel.setText(timeFormat.format(new Date(stats.exit.millis)));
            } else {
                timeLabel.setText("");
            }
            if (!stats.altitude.isEmpty()) {
                altitudeLabel.setText(Convert.distance(stats.altitude.max - stats.altitude.min));
            } else {
                altitudeLabel.setText("");
            }
            if (stats.exit != null) {
                horizontalDistLabel.setText(Convert.distance(stats.exit.distanceTo(stats.land)));
                verticalDistLabel.setText(Convert.distance(stats.altitude.range()));
            } else {
                horizontalDistLabel.setText("");
                verticalDistLabel.setText("");
            }
        } else {
            timeLabel.setText("");
            altitudeLabel.setText("");
            horizontalDistLabel.setText("");
            verticalDistLabel.setText("");
        }
        horizontalSpeedLabel.setText("");
        verticalSpeedLabel.setText("");
        speedLabel.setText("");
        glideLabel.setText("");
    }
}
