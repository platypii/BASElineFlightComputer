package com.platypii.baseline.views.charts;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.views.tracks.TrackDataActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TimeChartFragment extends Fragment {

    private TimeChart timeChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        timeChart = new TimeChartTouchable(getContext(), null);
        // Get track data from parent activity
        final Activity parent = getActivity();
        if (parent instanceof TrackDataActivity) {
            ((TrackDataActivity) parent).trackData.thenAccept(trackData -> {
                timeChart.loadTrack(trackData.data);
                timeChart.postInvalidate();
            });
        }
        return timeChart;
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
    public void onChartFocus(@NonNull ChartFocusEvent event) {
        timeChart.onFocus(event.location);
    }

}
