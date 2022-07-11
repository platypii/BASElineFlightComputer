package com.platypii.baseline.views.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.MapMenuBinding;
import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.location.PlaceAdapter;
import com.platypii.baseline.places.Place;

public class MapMenuFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "MapMenu";

    private MapMenuBinding binding;
    private PlaceAdapter listAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MapMenuBinding.inflate(inflater, container, false);

        // Button listeners
        binding.mode.setOnClickListener(modeListener);
        binding.layers.setOnClickListener(layerListener);
        binding.home.setOnClickListener(homeListener);
        binding.searchBox.addTextChangedListener(searchListener);
        binding.searchClear.setOnClickListener(searchClearListener);

        // Initialize the ListAdapter
        listAdapter = new PlaceAdapter(getContext());
        binding.searchResults.setAdapter(listAdapter);
        binding.searchResults.setOnItemClickListener(this);
        binding.getRoot().setOnTouchListener(this::onTouch);
        binding.searchBox.setOnFocusChangeListener(this::onSearchFocusChange);

        // Switch to navigation mode when in flight
        if (FlightMode.isFlight(Services.flightComputer.flightMode)) {
            MapState.menuOpen = false;
        }

        updateMenu(false);

        return binding.getRoot();
    }

    private void onSearchFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            final MapActivity mapActivity = getMapActivity();
            if (mapActivity != null) {
                // Hide soft keyboard
                final InputMethodManager imm = (InputMethodManager) mapActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private boolean onTouch(View view, MotionEvent motionEvent) {
        if (view != binding.searchBox) {
            binding.searchBox.clearFocus();
        }
        return false;
    }

    private void updateMenu(boolean animate) {
        if (MapState.menuOpen) {
            binding.mode.setImageResource(R.drawable.map_nav);
            binding.searchBox.setVisibility(View.VISIBLE);
            binding.searchClear.setVisibility(View.VISIBLE);
            final String searchString = binding.searchBox.getText().toString();
            if (searchString.isEmpty()) {
                binding.searchResults.setVisibility(View.GONE);
            } else {
                binding.searchResults.setVisibility(View.VISIBLE);
            }
            binding.layers.setVisibility(View.VISIBLE);
            binding.home.setVisibility(View.VISIBLE);
            binding.crosshair.setVisibility(View.VISIBLE);
            if (animate) {
                binding.searchBox.animate().translationX(0);
                binding.searchClear.animate().translationX(0);
                binding.searchResults.animate().translationX(0);
                binding.searchResults.animate().translationY(0);
                binding.layers.animate().translationY(0);
                binding.home.animate().translationY(0);
            } else {
                binding.searchBox.setTranslationX(0);
                binding.searchClear.setTranslationX(0);
                binding.searchResults.setTranslationX(0);
                binding.searchResults.setTranslationY(0);
                binding.layers.setTranslationY(0);
                binding.home.setTranslationY(0);
            }
        } else {
            binding.mode.setImageResource(R.drawable.gears);
            binding.searchBox.animate()
                    .translationX(-binding.searchBox.getWidth())
                    .withEndAction(() -> binding.searchBox.setVisibility(View.GONE));
            binding.searchClear.animate()
                    .translationX(-binding.searchBox.getWidth())
                    .withEndAction(() -> binding.searchClear.setVisibility(View.GONE));
            binding.searchResults.animate()
                    .translationX(-binding.searchResults.getWidth())
                    .translationY(-binding.searchResults.getHeight())
                    .withEndAction(() -> binding.searchResults.setVisibility(View.GONE));
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
        if (MapState.menuOpen) {
            // Close the menu
            final MapActivity mapActivity = getMapActivity();
            if (mapActivity != null) {
                // Recenter on last location
                mapActivity.resetLastDrag();
            }
            // Unfocus and hide soft keyboard
            binding.searchBox.clearFocus();
        }

        // Roll out map menu
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
                    final MapActivity mapActivity = getMapActivity();
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
        final MapActivity mapActivity = getMapActivity();
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

    @NonNull
    private final TextWatcher searchListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            final String searchString = binding.searchBox.getText().toString();
            if (searchString.isEmpty()) {
                binding.searchResults.setVisibility(View.GONE);
                binding.searchClear.setImageResource(R.drawable.search_button);
                binding.searchClear.setClickable(false);
            } else {
                Log.i(TAG, "Searching for: " + searchString);
                binding.searchResults.setVisibility(View.VISIBLE);
                binding.searchClear.setImageResource(R.drawable.search_clear);
                binding.searchClear.setClickable(true);
                listAdapter.setFilter(searchString);
            }
        }
    };

    @NonNull
    private final View.OnClickListener searchClearListener = view -> {
        binding.searchBox.setText("");
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Place place = listAdapter.getItem(position);
        Log.i(TAG, "Map search click " + place);
        final MapActivity mapActivity = getMapActivity();
        if (mapActivity != null) {
            // Zoom to location
            mapActivity.setCenter(place.latLng(), 14);
        }
        // Unfocus and hide soft keyboard
        binding.searchBox.clearFocus();
    }

    @Nullable
    private MapActivity getMapActivity() {
        final Activity activity = getActivity();
        return activity instanceof MapActivity ? (MapActivity) activity : null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restore map state
        binding.searchBox.setText(MapState.searchString);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save map state
        MapState.searchString = binding.searchBox.getText().toString();
    }

}
