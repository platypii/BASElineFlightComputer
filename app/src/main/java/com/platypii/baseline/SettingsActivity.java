package com.platypii.baseline;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.platypii.baseline.data.Convert;

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

}
