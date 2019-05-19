package com.platypii.baseline.places;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlaceTest {

    private final Place kpow = new Place("Kpow", "", "USA", 47.239, -123.143, 84.2, "DZ", 30000, false);

    @Test
    public void place() {
        assertEquals("Kpow", kpow.name);
        assertEquals("USA", kpow.country);
        assertEquals(47.239, kpow.latLng().latitude, 0.001);
        assertEquals(-123.143, kpow.latLng().longitude, 0.001);
        assertEquals("Kpow", kpow.toString());
    }

}
