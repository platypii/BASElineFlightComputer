package com.platypii.baseline;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Displays a polar plot
 */
public class PolarFragment extends Fragment {

    private PolarPlot polar;

    public PolarFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        polar = new PolarPlot(getActivity(), null);
        return polar;
    }

    @Override
    public void onResume() {
        super.onResume();
        polar.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        polar.stop();
    }

}
