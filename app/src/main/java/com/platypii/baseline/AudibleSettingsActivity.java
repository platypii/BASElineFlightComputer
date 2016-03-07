package com.platypii.baseline;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import com.platypii.baseline.audible.MyAudible;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
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
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class AudiblePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private SwitchPreference enabledPreference;
        private ListPreference modePreference;
        private EditTextPreference minPreference;
        private EditTextPreference maxPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_audible);
            setHasOptionsMenu(true);

            enabledPreference = (SwitchPreference) findPreference("audible_enabled");
            modePreference = (ListPreference) findPreference("audible_mode");
            minPreference = (EditTextPreference) findPreference("audible_min");
            maxPreference = (EditTextPreference) findPreference("audible_max");

            enabledPreference.setOnPreferenceChangeListener(this);
            modePreference.setOnPreferenceChangeListener(this);
            minPreference.setOnPreferenceChangeListener(this);
            maxPreference.setOnPreferenceChangeListener(this);

            updateViews();
        }

        /**
         * Set summaries and adjust defaults
         */
        private void updateViews() {
            // Audible mode
            final String audibleMode = modePreference.getValue();
            final int modePreferenceIndex = modePreference.findIndexOfValue(audibleMode);
            final CharSequence modeValue = modePreferenceIndex >= 0? modePreference.getEntries()[modePreferenceIndex] : null;
            modePreference.setSummary(modeValue);

            final double min = Double.parseDouble(minPreference.getText());
            final double max = Double.parseDouble(maxPreference.getText());
            switch(audibleMode) {
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

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            if (preference.getKey().equals("audible_enabled")) {
                final boolean audibleEnabled = (Boolean) value;
                if(audibleEnabled) {
                    MyAudible.startAudible();
                } else {
                    MyAudible.stopAudible();
                }
            } else if (preference.getKey().equals("audible_mode")) {
                final String audibleMode = (String) value;
                switch(audibleMode) {
                    case "horizontal_speed":
                        // Set default min/max
                        minPreference.setText("60.0");
                        maxPreference.setText("120.0");
                        break;
                    case "vertical_speed":
                        // Set default min/max
                        minPreference.setText("30.0");
                        maxPreference.setText("120.0");
                        break;
                    case "glide_ratio":
                        // Set default min/max
                        minPreference.setText("0.0");
                        maxPreference.setText("3.0");
                        break;
                    default:
                        Log.e(TAG, "Invalid audible mode " + audibleMode);
                }
            }

            updateViews();

            return true;
        }

    }

}
