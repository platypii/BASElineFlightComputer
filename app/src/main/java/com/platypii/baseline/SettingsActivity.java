package com.platypii.baseline;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.platypii.baseline.data.Convert;
import com.platypii.baseline.util.Util;

/**
 * Settings activity for things like metric / imperial
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    /**
     * This fragment shows the preferences
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private CheckBoxPreference metricPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            metricPreference = (CheckBoxPreference) findPreference("metric_enabled");
            metricPreference.setOnPreferenceChangeListener(this);

            updateViews();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            switch(preference.getKey()) {
                case "metric_enabled":
                    final boolean metricEnabled = (Boolean) value;
                    Log.i(TAG, "Setting metric mode: " + metricEnabled);
                    Convert.metric = metricEnabled;

                    // Convert audible min and max
                    final SharedPreferences prefs = preference.getSharedPreferences();
                    double audible_min = Util.parseDouble(prefs.getString("audible_min", null));
                    double audible_max = Util.parseDouble(prefs.getString("audible_max", null));
                    final SharedPreferences.Editor edit = prefs.edit();
                    if(metricEnabled) {
                        // Convert mph -> km/h
                        audible_min = Convert.mph2kph(audible_min);
                        audible_max = Convert.mph2kph(audible_max);
                    } else {
                        audible_min = Convert.kph2mph(audible_min);
                        audible_max = Convert.kph2mph(audible_max);
                    }
                    edit.putString("audible_min", Double.toString(audible_min));
                    edit.putString("audible_max", Double.toString(audible_max));
                    edit.apply();

                    break;
            }
            updateViews();
            return true;
        }

        private void updateViews() {
            if(Convert.metric) {
                metricPreference.setSummary("Current units: metric");
            } else {
                metricPreference.setSummary("Current units: imperial");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        // Stop flight services
        Services.stop();
    }

}
