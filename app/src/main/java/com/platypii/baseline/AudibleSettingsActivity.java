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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.platypii.baseline.audible.AudibleMinMaxPreference;
import com.platypii.baseline.audible.AudibleMode;
import com.platypii.baseline.audible.AudibleSettings;
import com.platypii.baseline.util.Util;
import java.util.Locale;

/**
 * Settings activity for audible configuration
 */
public class AudibleSettingsActivity extends PreferenceActivity {

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

        private FirebaseAnalytics firebaseAnalytics;

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

            firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

            updateViews();
        }

        /**
         * Set summaries and adjust defaults
         */
        private void updateViews() {
            final AudibleMode mode = AudibleSettings.mode;
            final int precision = AudibleSettings.precision;
            modePreference.setSummary(mode.name);
            minPreference.setTitle(mode.minimumTitle());
            maxPreference.setTitle(mode.maximumTitle());
            minPreference.setSummary(mode.renderDisplay(AudibleSettings.min, precision));
            maxPreference.setSummary(mode.renderDisplay(AudibleSettings.max, precision));
            if(precision <= 0) {
                precisionPreference.setSummary(String.format(Locale.getDefault(), "%d decimal places", precision));
            } else {
                precisionPreference.setSummary(String.format(Locale.getDefault(), "%."+precision+"f decimal places", (float) precision));
            }
            intervalPreference.setSummary("Every " + AudibleSettings.speechInterval + " sec");
            ratePreference.setSummary(String.format(Locale.getDefault(), "%.2fx", AudibleSettings.speechRate));
        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object value) {
            final String key = preference.getKey();
            switch(key) {
                case "audible_enabled":
                    final boolean audibleEnabled = (Boolean) value;
                    if(audibleEnabled) {
                        firebaseAnalytics.logEvent("pref_start_audible", null);
                        Services.audible.enableAudible();
                    } else {
                        firebaseAnalytics.logEvent("pref_stop_audible", null);
                        Services.audible.disableAudible();
                    }
                    Notifications.updateNotification(getActivity());
                    break;
                case "audible_mode":
                    final String audibleMode = (String) value;
                    final String previousAudibleMode = modePreference.getValue();
                    if(!audibleMode.equals(previousAudibleMode)) {
                        AudibleSettings.setAudibleMode(audibleMode);
                        minPreference.setValue(AudibleSettings.mode.defaultMin);
                        maxPreference.setValue(AudibleSettings.mode.defaultMax);
                        precisionPreference.setDefaultValue(AudibleSettings.mode.defaultPrecision);
                        updateViews();
                        if(Services.audible.isEnabled()) {
                            Services.audible.speakNow(AudibleSettings.mode.name);
                        }
                    }
                    break;
                case "audible_min":
                    final float min = (Float) value;
                    if(!Util.isReal(min)) return false;
                    AudibleSettings.min = min;
                    updateViews();
                    break;
                case "audible_max":
                    final float max = (Float) value;
                    if(!Util.isReal(max)) return false;
                    AudibleSettings.max = max;
                    updateViews();
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
                    AudibleSettings.precision = precision;
                    updateViews();
                    break;
                case "audible_interval":
                    final float speechInterval = Util.parseFloat((String) value);
                    if(!Util.isReal(speechInterval)) return false;
                    AudibleSettings.speechInterval = speechInterval;
                    updateViews();
                    break;
                case "audible_rate":
                    final float speechRate = Util.parseFloat((String) value);
                    if(!Util.isReal(speechRate)) return false;
                    AudibleSettings.speechRate = speechRate;
                    updateViews();
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
