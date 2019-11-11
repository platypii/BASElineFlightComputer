package com.platypii.baseline.views.charts;

import com.platypii.baseline.views.map.TrackMapFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ChartPagerAdapter extends FragmentPagerAdapter {
    ChartPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new TimeChartFragment();
            case 1:
                return new FlightProfileFragment();
            case 2:
                return new SpeedChartFragment();
            case 3:
                return new TrackMapFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Time";
            case 1:
                return "Distance";
            case 2:
                return "Speed";
            case 3:
                return "Map";
            default:
                return null;
        }
    }
}
