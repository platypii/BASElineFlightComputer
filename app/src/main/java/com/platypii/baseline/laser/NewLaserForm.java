package com.platypii.baseline.laser;

import com.platypii.baseline.util.Convert;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

public class NewLaserForm {
    public final String name;
    public final boolean metric;
    public final String lat;
    public final String lng;
    public final String alt;
    public final String points;

    public NewLaserForm(String name, boolean metric, String lat, String lng, String alt, String points) {
        this.name = name;
        this.metric = metric;
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.points = points;
    }

    public void save(@NonNull Context context) {
        final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString("new_laser_form.name", name);
        edit.putBoolean("new_laser_form.metric", metric);
        edit.putString("new_laser_form.lat", lat);
        edit.putString("new_laser_form.lng", lng);
        edit.putString("new_laser_form.alt", alt);
        edit.putString("new_laser_form.points", points);
        edit.apply();
    }

    public static NewLaserForm load(@NonNull Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new NewLaserForm(
            prefs.getString("new_laser_form.name", ""),
            prefs.getBoolean("new_laser_form.metric", Convert.metric),
            prefs.getString("new_laser_form.lat", ""),
            prefs.getString("new_laser_form.lng", ""),
            prefs.getString("new_laser_form.alt", ""),
            prefs.getString("new_laser_form.points", "")
        );
    }

}
