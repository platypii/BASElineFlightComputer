package com.platypii.baseline.views.map;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.MapMenuBinding;
import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.location.LandingZone;

public class MapMenuFragment extends Fragment {

    private MapMenuBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MapMenuBinding.inflate(inflater, container, false);

        // Button listeners
        binding.mode.setOnClickListener(modeListener);
        binding.layers.setOnClickListener(layerListener);
        binding.home.setOnClickListener(homeListener);

        // Switch to navigation mode when in flight
        if (FlightMode.isFlight(Services.flightComputer.flightMode)) {
            MapState.menuOpen = false;
        }

        updateMenu(false);

        return binding.getRoot();
    }

    private void updateMenu(boolean animate) {
        if (MapState.menuOpen) {
            binding.mode.setImageResource(R.drawable.map_nav);
            binding.layers.setVisibility(View.VISIBLE);
            binding.home.setVisibility(View.VISIBLE);
            binding.crosshair.setVisibility(View.VISIBLE);
            if (animate) {
                binding.layers.animate().translationY(0);
                binding.home.animate().translationY(0);
            } else {
                binding.layers.setTranslationY(0);
                binding.home.setTranslationY(0);
            }
        } else {
            binding.mode.setImageResource(R.drawable.gears);
            binding.layers.animate()
                    .translationY(-binding.layers.getHeight())
                    .withEndAction(() -> binding.layers.setVisibility(View.GONE));
            binding.home.animate()
                    .translationY(-2 * binding.home.getHeight())
                    .withEndAction(() -> binding.home.setVisibility(View.GONE));
            binding.crosshair.setVisibility(View.GONE);
        }
    }

    @NonNull
    private final View.OnClickListener modeListener = view -> {
        // Roll out map options
        MapState.menuOpen = !MapState.menuOpen;
        updateMenu(true);
    };

    @NonNull
    private final View.OnClickListener layerListener = view -> {
        final String[] layers = {
                "Exits",
                "Dropzones",
//                "Launches"
        };
        final boolean[] checked = {
                MapState.showExits,
                MapState.showDropzones,
//                PlacesLayerOptions.showLaunches
        };
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.layers)
                .setMultiChoiceItems(layers, checked, (dialog, which, isChecked) -> {
                    if (which == 0) {
                        MapState.showExits = isChecked;
                    } else if (which == 1) {
                        MapState.showDropzones = isChecked;
                    } else if (which == 2) {
                        MapState.showLaunches = isChecked;
                    }
                    final MapActivity mapActivity = (MapActivity) getActivity();
                    if (mapActivity != null) {
                        mapActivity.updatePlacesLayer();
                    }
                })
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    };

    @NonNull
    private final View.OnClickListener homeListener = view -> {
        final MapActivity mapActivity = (MapActivity) getActivity();
        if (mapActivity != null) {
            final LatLng center = mapActivity.getCenter();
            if (center != null) {
                if (center.equals(LandingZone.homeLoc)) {
                    // Dropped pin on exact same location, delete home
                    mapActivity.setHome(null);
                } else {
                    // Set home location to map center
                    mapActivity.setHome(center);
                }
            }
        }
    };

}
