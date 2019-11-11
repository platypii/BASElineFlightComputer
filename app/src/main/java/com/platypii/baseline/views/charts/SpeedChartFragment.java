package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.views.tracks.TrackDataActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SpeedChartFragment extends Fragment {

    @Nullable
    private SpeedChart speedChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        speedChart = new SpeedChartTouchable(getContext(), null);
        // Get track data from parent activity
        final Activity parent = getActivity();
        if (parent instanceof TrackDataActivity) {
            ((TrackDataActivity) parent).trackData.thenAccept(trackData -> {
                speedChart.loadTrack(trackData);
                speedChart.postInvalidate();
            });
        }
        return speedChart;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        speedChart = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChartFocus(@NonNull ChartFocusEvent event) {
        if (speedChart != null) {
            speedChart.onFocus(event.location);
        }
    }

}
