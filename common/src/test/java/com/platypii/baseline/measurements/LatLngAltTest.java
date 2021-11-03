package com.platypii.baseline.measurements;

import com.platypii.baseline.util.Convert;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LatLngAltTest {

    private final LatLngAlt ll = new LatLngAlt(47.239, -123.143, 84);

    @Test
    public void formatLatLng() {
        Convert.metric = true;
        assertEquals("47.239, -123.143", LatLngAlt.formatLatLng(ll.lat, ll.lng));
    }

    @Test
    public void formatLatLngAlt() {
        Convert.metric = true;
        assertEquals("47.239, -123.143, 84 m", LatLngAlt.formatLatLngAlt(ll.lat, ll.lng, ll.alt));
    }

    @Test
    public void stringify() {
        Convert.metric = true;
        assertEquals("47.239, -123.143, 84 m", ll.toString());
    }

}
