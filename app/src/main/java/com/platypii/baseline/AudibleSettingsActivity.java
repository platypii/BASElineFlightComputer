package com.platypii.baseline;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.util.Log;

import com.platypii.baseline.audible.MyAudible;

/**
 * Settings activity for audible configuration
 */
public class AudibleSettingsActivity extends PreferenceActivity {
    private static final String TAG = "AudibleSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AudiblePreferenceFragment())
                .commit();
    }

    /**
     * This fragment shows the preferences
     */
    public static class AudiblePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private SwitchPreference enabledPreference;
        private ListPreference modePreference;
        private EditTextPreference minPreference;
        private EditTextPreference maxPreference;
        private EditTextPreference ratePreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_audible);
            setHasOptionsMenu(true);

            enabledPreference = (SwitchPreference) findPreference("audible_enabled");
            modePreference = (ListPreference) findPreference("audible_mode");
            minPreference = (EditTextPreference) findPreference("audible_min");
            maxPreference = (EditTextPreference) findPreference("audible_max");
            ratePreference = (EditTextPreference) findPreference("audible_rate");

            enabledPreference.setOnPreferenceChangeListener(this);
            modePreference.setOnPreferenceChangeListener(this);
            minPreference.setOnPreferenceChangeListener(this);
            maxPreference.setOnPreferenceChangeListener(this);
            ratePreference.setOnPreferenceChangeListener(this);

            updateViews();
        }

        /**
         * Set summaries and adjust defaults
         */
        private void updateViews() {
            // Read preferences
            final String audibleMode = modePreference.getValue();
            final double min = Double.parseDouble(minPreference.getText());
            final double max = Double.parseDouble(maxPreference.getText());
            final double speechRate = Double.parseDouble(ratePreference.getText());
            // Update views
            updateAudibleMode(audibleMode, min, max);
            updateSpeechRate(speechRate);
        }

        private void updateAudibleMode(String audibleMode, double min, double max) {
            Log.d(TAG, "Updating preference views");

            // Audible mode
            final CharSequence modeValue = getAudibleModeValue(audibleMode);
            modePreference.setSummary(modeValue);

            // Minimum and maximum values
            switch (audibleMode) {
                case "glide_ratio":
                    minPreference.setTitle("Minimum Glide Ratio");
                    maxPreference.setTitle("Maximum Glide Ratio");
                    minPreference.setSummary(String.format("%.2f", min) + " : 1");
                    maxPreference.setSummary(String.format("%.2f", max) + " : 1");
                    break;
                case "horizontal_speed":
                case "vertical_speed":
                    // Set units
                    minPreference.setTitle("Minimum Speed");
                    maxPreference.setTitle("Maximum Speed");
                    minPreference.setSummary(String.format("%.0f", min) + " mph");
                    maxPreference.setSummary(String.format("%.0f", max) + " mph");
                    break;
                default:
                    Log.e(TAG, "Invalid audible mode " + audibleMode);
            }
        }

        private void updateSpeechRate(double speechRate) {
            ratePreference.setSummary("Every " + speechRate + " sec");
        }

        /**
         * Gets the human readable audible mode from the id
         * glide_ratio -> Glide Ratio
         * @param mode the audible mode string
         * @return a human readable audible mode string
         */
        private CharSequence getAudibleModeValue(String mode) {
            final int modePreferenceIndex = modePreference.findIndexOfValue(mode);
            return modePreferenceIndex >= 0? modePreference.getEntries()[modePreferenceIndex] : mode;
        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object value) {
            final String key = preference.getKey();
            final String previousAudibleMode = modePreference.getValue();
            final double previousMin = Double.parseDouble(minPreference.getText());
            final double previousMax = Double.parseDouble(maxPreference.getText());
            switch(key) {
                case "audible_enabled":
                    final boolean audibleEnabled = (Boolean) value;
                    if(audibleEnabled) {
                        MyAudible.startAudible();
                    } else {
                        MyAudible.stopAudible();
                    }
                    break;
                case "audible_mode":
                    final String audibleMode = (String) value;
                    if(!audibleMode.equals(previousAudibleMode)) {
                        double min = 0;
                        double max = 120;
                        switch (audibleMode) {
                            case "horizontal_speed":
                            case "vertical_speed":
                                // Set default min/max
                                min = 0;
                                max = 140;
                                break;
                            case "glide_ratio":
                                // Set default min/max
                                min = 0;
                                max = 3;
                                break;
                            default:
                                Log.e(TAG, "Invalid audible mode " + audibleMode);
                        }
                        minPreference.setText(Double.toString(min));
                        maxPreference.setText(Double.toString(max));
                        updateAudibleMode(audibleMode, min, max);
                    }
                    break;
                case "audible_min":
                    final double min = Double.parseDouble((String) value);
                    updateAudibleMode(previousAudibleMode, min, previousMax);
                    break;
                case "audible_max":
                    final double max = Double.parseDouble((String) value);
                    updateAudibleMode(previousAudibleMode, previousMin, max);
                    break;
                case "audible_rate":
                    final double speechRate = Double.parseDouble((String) value);
                    updateSpeechRate(speechRate);
                    break;
            }

            return true;
        }

    }

}
