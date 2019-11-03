package com.platypii.baseline.audible;

import com.platypii.baseline.SharedPreferencesMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AudibleSettingsTest {

    @Test
    public void loadSettings() {
        AudibleSettings settings = new AudibleSettings();
        settings.load(new SharedPreferencesMock());

        assertFalse(settings.isEnabled);
        assertTrue(settings.airplaneMode);
        assertEquals("Horizontal Speed", settings.mode.name);
        assertEquals(0.0, settings.min, 0.001);
        assertEquals(80.467, settings.max, 0.001);
        assertEquals(0, settings.precision);
        assertEquals(2.5, settings.speechInterval, 0.001);
        assertEquals(1.0, settings.speechRate, 0.001);
    }

    @Test
    public void setAudibleMode() {
        AudibleSettings settings = new AudibleSettings();
        settings.load(new SharedPreferencesMock());
        settings.setAudibleMode("glide_ratio");

        assertFalse(settings.isEnabled);
        assertTrue(settings.airplaneMode);
        assertEquals("Glide Ratio", settings.mode.name);
        assertEquals(0.0, settings.min, 0.001);
        assertEquals(4.0, settings.max, 0.001);
    }
}
