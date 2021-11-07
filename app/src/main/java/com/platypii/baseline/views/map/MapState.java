package com.platypii.baseline.views.map;

import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapState {

    public static boolean showExits = true;
    public static boolean showDropzones = true;
    public static boolean showLaunches = true;

    @Nullable
    public static LatLngBounds mapBounds;

}
