package com.platypii.baseline.views.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;
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

    public static void load(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        showExits = prefs.getBoolean("map_show_exits", showExits);
        showDropzones = prefs.getBoolean("map_show_dropzones", showDropzones);
        showLaunches = prefs.getBoolean("map_show_launches", showLaunches);

        if (prefs.contains("map_bounds_north")) {
            final float north = prefs.getFloat("map_bounds_north", Float.NaN);
            final float east = prefs.getFloat("map_bounds_east", Float.NaN);
            final float south = prefs.getFloat("map_bounds_south", Float.NaN);
            final float west = prefs.getFloat("map_bounds_west", Float.NaN);
            mapBounds = new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
        }
    }

    public static void save(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("map_show_exits", showExits);
        editor.putBoolean("map_show_dropzones", showDropzones);
        editor.putBoolean("map_show_launches", showLaunches);
        if (mapBounds != null) {
            editor.putFloat("map_bounds_north", (float) mapBounds.northeast.latitude);
            editor.putFloat("map_bounds_east", (float) mapBounds.northeast.longitude);
            editor.putFloat("map_bounds_south", (float) mapBounds.southwest.latitude);
            editor.putFloat("map_bounds_west", (float) mapBounds.southwest.longitude);
        }
        editor.apply();
    }

}
