package com.platypii.baseline.lasers;

import com.platypii.baseline.util.Convert;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

public class NewLaserForm {
    public final String name;
    public final boolean metric;
    public final String latLngAlt;
    public final String points;

    public NewLaserForm(String name, boolean metric, String latLngAlt, String points) {
        this.name = name;
        this.metric = metric;
        this.latLngAlt = latLngAlt;
        this.points = points;
    }

    public void save(@NonNull Context context) {
        final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (name.isEmpty() && points.isEmpty()) {
            edit.remove("new_laser_form.name");
            edit.remove("new_laser_form.metric");
            edit.remove("new_laser_form.latlngalt");
            edit.remove("new_laser_form.points");
        } else {
            edit.putString("new_laser_form.name", name);
            edit.putBoolean("new_laser_form.metric", metric);
            edit.putString("new_laser_form.latlngalt", latLngAlt);
            edit.putString("new_laser_form.points", points);
        }
        edit.apply();
    }

    @NonNull
    public static NewLaserForm load(@NonNull Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new NewLaserForm(
                prefs.getString("new_laser_form.name", ""),
                prefs.getBoolean("new_laser_form.metric", Convert.metric),
                prefs.getString("new_laser_form.latlngalt", ""),
                prefs.getString("new_laser_form.points", "")
        );
    }

}
