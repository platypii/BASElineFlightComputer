package com.platypii.baseline.ui;

import com.platypii.baseline.audible.EventsActivity;
import com.platypii.baseline.R;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getActionBar().hide();

        // Start ground level activity on-click
        Preference groundLevelPref = findPreference("groundLevelPref");
        groundLevelPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
                // Open ground level activity
                startActivity(new Intent(SettingsActivity.this, GroundLevelActivity.class));
				return false;
			}
        });

        // Start audible settings activity on-click
        Preference audiblePref = findPreference("audiblePref");
        audiblePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
		        // Open audible activity
				startActivity(new Intent(SettingsActivity.this, EventsActivity.class));
				return false;
			}
        });

        // Start sensors activity on-click
        Preference sensorsPref = findPreference("sensorsPref");
        sensorsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
		        // Open sensors activity
				startActivity(new Intent(SettingsActivity.this, SensorActivity.class));
				return false;
			}
        });

        // Update units
        CheckBoxPreference metricPref = (CheckBoxPreference) findPreference("metricPref");
        metricPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// Update Convert class
				Convert.setMetric((Boolean) newValue);
				return true;
			}
        });
    }

}



