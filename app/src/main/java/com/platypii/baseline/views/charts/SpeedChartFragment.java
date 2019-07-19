package com.platypii.baseline.views.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.tracks.TrackData;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SpeedChartFragment extends Fragment {

    private PolarPlot speedChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        speedChart = new PolarPlotTouchable(getContext(), null);
        // Get track data from parent fragment
        final Fragment parent = getParentFragment();
        if (parent instanceof ChartsFragment) {
            ((ChartsFragment) parent).trackData.thenAccept((TrackData trackData) -> {
                speedChart.loadTrack(trackData.data);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChartFocus(ChartFocusEvent event) {
        speedChart.onFocus(event.location);
    }

}
