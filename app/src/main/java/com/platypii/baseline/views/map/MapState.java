package com.platypii.baseline.views.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapState {

    public static boolean menuOpen = true;

    public static boolean showExits = true;
    public static boolean showDropzones = true;
    public static boolean showLaunches = true;

    @NonNull
    public static String searchString = "";

    @Nullable
    public static LatLngBounds mapBounds;

}
