package com.platypii.baseline.location;

import com.platypii.baseline.measurements.LatLngAlt;

import androidx.annotation.Nullable;
import java.text.ParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are parsing NMEA correctly
 */
public class GeocoderTest {

    @Test
    public void parse() throws ParseException {
        final LatLngAlt lla = Geocoder.parse("12.3456, -78.9012, 66ft");
        assertEquals(12.3456, lla.lat, 0.00001);
        assertEquals(-78.9012, lla.lng, 0.00001);
        assertEquals(20.11, lla.alt, 0.01);
    }

    @Test
    public void parseAltitude() {
        assertEquals(-100.0, alt("1.2, 3.4, -100"), 0.01);
        assertEquals(0.0, alt("1.2, 3.4, 0"), 0.01);
        assertEquals(100.0, alt("1.2, 3.4, 100"), 0.01);
        assertEquals(100.0, alt("1.2, 3.4, 100.0"), 0.01);
        assertEquals(100.0, alt("1.2, 3.4, 100m"), 0.01);
        assertEquals(30.48, alt("1.2, 3.4, 100ft"), 0.01);
        assertEquals(100.0, alt("1.2, 3.4, 100 m"), 0.01);
        assertEquals(30.48, alt("1.2, 3.4, 100 ft"), 0.01);
    }

    @Test
    public void parseErrors() {
        assertEquals("Missing latitude, longitude, altitude", error(""));
        assertEquals("Invalid latitude, longitude, altitude", error("1"));
        assertEquals("Invalid latitude, longitude, altitude", error("1.2"));
        assertEquals("Missing altitude", error("1.2, 3"));
        assertEquals("Missing longitude, altitude", error("1.2, "));
        assertEquals("Missing longitude, altitude", error("1.2, ,"));
        assertEquals("Missing altitude", error("1.2, 3.4"));
        assertEquals("Missing altitude", error("1.2, 3.4, "));
        assertEquals("Invalid altitude: ft", error("1.2, 3.4, ft"));
        assertEquals("Missing latitude", error(", 3.4, 56"));
        assertEquals("Invalid latitude, longitude, altitude", error(", 3.4"));
        assertEquals("Invalid latitude: X", error("X, 3.4, 56"));
        assertEquals("Missing longitude", error("1.2, , 56"));
        assertEquals("Invalid longitude: X", error("1.2, X, 56"));
        assertEquals("Invalid altitude: X", error("1.2, 3.4, X"));
        assertEquals("Invalid longitude: 300.4 is not between -180 and 180", error("80.2, 300.4, 56"));
        assertEquals("Invalid latitude: -90.2 is not between -90 and 90", error("-90.2, -190.4, 56"));
        assertEquals("Invalid latitude, longitude, altitude", error("1.2, 3.4, 1, 2"));
        assertEquals("Invalid altitude: 1 2", error("1.2, 3.4, 1 2"));
        assertEquals("Invalid altitude: NaN", error("1.2, 3.4, NaN"));
        assertEquals("Invalid altitude: Infinity", error("1.2, 3.4, Infinity"));
    }

    private double alt(String str) {
        try {
            return Geocoder.parse(str).alt;
        } catch (ParseException e) {
            return Double.NaN;
        }
    }

    @Nullable
    private String error(String str) {
        try {
            Geocoder.parse(str);
            return null;
        } catch (ParseException e) {
            return e.getMessage();
        }
    }

}
