package com.platypii.baseline;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Displays a polar plot
 */
public class PolarFragment2 extends Fragment {

    private PolarPlot2 polar;

    public PolarFragment2() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        polar = new PolarPlot2(getActivity(), null);
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
