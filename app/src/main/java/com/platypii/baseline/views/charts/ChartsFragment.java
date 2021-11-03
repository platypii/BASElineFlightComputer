package com.platypii.baseline.views.charts;

import com.platypii.baseline.databinding.ChartsFragBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChartsFragment extends Fragment {
    private ChartsFragBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ChartsFragBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ChartPagerAdapter chartPagerAdapter = new ChartPagerAdapter(getChildFragmentManager());
        binding.chartPager.setAdapter(chartPagerAdapter);
    }

}
