package com.platypii.baseline;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.platypii.baseline.audible.AudibleMinMaxPreference;
import com.platypii.baseline.audible.AudibleMode;
import com.platypii.baseline.audible.AudibleModes;
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.util.Util;
import java.util.Locale;

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
        private AudibleMinMaxPreference minPreference;
        private AudibleMinMaxPreference maxPreference;
        private EditTextPreference precisionPreference;
        private EditTextPreference intervalPreference;
        private EditTextPreference ratePreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_audible);
            setHasOptionsMenu(true);

            enabledPreference = (SwitchPreference) findPreference("audible_enabled");
            modePreference = (ListPreference) findPreference("audible_mode");
            minPreference = (AudibleMinMaxPreference) findPreference("audible_min");
            maxPreference = (AudibleMinMaxPreference) findPreference("audible_max");
            precisionPreference = (EditTextPreference) findPreference("audible_precision");
            intervalPreference = (EditTextPreference) findPreference("audible_interval");
            ratePreference = (EditTextPreference) findPreference("audible_rate");

            enabledPreference.setOnPreferenceChangeListener(this);
            modePreference.setOnPreferenceChangeListener(this);
            minPreference.setOnPreferenceChangeListener(this);
            maxPreference.setOnPreferenceChangeListener(this);
            precisionPreference.setOnPreferenceChangeListener(this);
            intervalPreference.setOnPreferenceChangeListener(this);
            ratePreference.setOnPreferenceChangeListener(this);

            updateViews();
        }

        /**
         * Set summaries and adjust defaults
         */
        private void updateViews() {
            // Read preferences
            final String audibleMode = modePreference.getValue();
            final float min = minPreference.getValue();
            final float max = maxPreference.getValue();
            final int precision = Util.parseInt(precisionPreference.getText(), 1);
            final double speechInterval = Util.parseDouble(intervalPreference.getText());
            final double speechRate = Util.parseDouble(ratePreference.getText());
            // Update views
            updateAudibleMode(audibleMode, min, max, precision);
            updateSpeechInterval(speechInterval);
            updateSpeechRate(speechRate);
        }

        private void updateAudibleMode(String audibleMode, float min, float max, int precision) {
            // Audible mode
            final AudibleMode mode = AudibleModes.get(audibleMode);

            modePreference.setSummary(mode.name);
            minPreference.setTitle(mode.minimumTitle());
            maxPreference.setTitle(mode.maximumTitle());
            minPreference.setSummary(mode.convertOutput(min, precision));
            maxPreference.setSummary(mode.convertOutput(max, precision));
            if(precision < 0) {
                precisionPreference.setSummary(String.format("%d decimal places", precision));
            } else {
                precisionPreference.setSummary(String.format("%."+precision+"f decimal places", (float) precision));
            }
        }

        private void updateSpeechInterval(double speechInterval) {
            intervalPreference.setSummary("Every " + speechInterval + " sec");
        }

        private void updateSpeechRate(double speechRate) {
            ratePreference.setSummary(String.format(Locale.getDefault(), "%.2fx", speechRate));
        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object value) {
            final String key = preference.getKey();
            final String previousAudibleMode = modePreference.getValue();
            final float previousMin = minPreference.getValue();
            final float previousMax = maxPreference.getValue();
            final int previousPrecision = Util.parseInt(precisionPreference.getText(), 1);
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
                        final AudibleMode mode = AudibleModes.get(audibleMode);
                        minPreference.setValue(mode.defaultMin);
                        maxPreference.setValue(mode.defaultMax);
                        precisionPreference.setDefaultValue(mode.defaultPrecision);
                        updateAudibleMode(audibleMode, mode.defaultMin, mode.defaultMax, mode.defaultPrecision);
                        if(MyAudible.isEnabled()) {
                            MyAudible.speakNow(mode.name);
                        }
                    }
                    break;
                case "audible_min":
                    final float min = (Float) value;
                    if(!Util.isReal(min)) return false;
                    updateAudibleMode(previousAudibleMode, min, previousMax, previousPrecision);
                    break;
                case "audible_max":
                    final float max = (Float) value;
                    if(!Util.isReal(max)) return false;
                    updateAudibleMode(previousAudibleMode, previousMin, max, previousPrecision);
                    break;
                case "audible_precision":
                    final int precision = Util.parseInt((String) value, 1);
                    if(precision < 0) {
                        Toast.makeText(getActivity(), "Precision cannot be negative", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if(8 < precision) {
                        Toast.makeText(getActivity(), "Precision cannot be greater than 8", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    updateAudibleMode(previousAudibleMode, previousMin, previousMax, precision);
                    break;
                case "audible_interval":
                    final double speechInterval = Util.parseDouble((String) value);
                    if(!Util.isReal(speechInterval)) return false;
                    updateSpeechInterval(speechInterval);
                    break;
                case "audible_rate":
                    final double speechRate = Util.parseDouble((String) value);
                    if(!Util.isReal(speechRate)) return false;
                    updateSpeechRate(speechRate);
                    break;
            }
            return true;
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
