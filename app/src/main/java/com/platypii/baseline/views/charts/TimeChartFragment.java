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

public class TimeChartFragment extends Fragment {

    @Nullable
    private TimeChart timeChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        timeChart = new TimeChartTouchable(getContext(), null);
        // Get track data from parent activity
        final Activity parent = getActivity();
        if (parent instanceof TrackDataActivity) {
            ((TrackDataActivity) parent).trackData.thenAccept(trackData -> {
                timeChart.loadTrack(trackData);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeChart = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackFocus(@NonNull ChartFocusEvent.TrackFocused event) {
        if (timeChart != null) {
            timeChart.onFocus(event.location);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnFocus(@NonNull ChartFocusEvent.Unfocused event) {
        if (timeChart != null) {
            timeChart.onFocus(null);
        }
    }

}
