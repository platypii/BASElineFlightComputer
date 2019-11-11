package com.platypii.baseline.views.charts;

import com.platypii.baseline.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class ChartsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.charts_frag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ChartPagerAdapter chartPagerAdapter = new ChartPagerAdapter(getChildFragmentManager());
        final ViewPager viewPager = view.findViewById(R.id.chartPager);
        viewPager.setAdapter(chartPagerAdapter);
    }

}
