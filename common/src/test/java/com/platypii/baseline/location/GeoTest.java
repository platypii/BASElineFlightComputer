package com.platypii.baseline.location;

import com.google.android.gms.maps.model.LatLng;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are parsing NMEA correctly
 */
public class GeoTest {

    private final LatLng seattle = new LatLng(47.60, -122.33);
    private final LatLng la = new LatLng(34.0, -118.2);

    private final double bearing = 165.66;
    private final double distance = 1551093.52;

    @Test
    public void bearing() {
        assertEquals(bearing, Geo.bearing(seattle.latitude, seattle.longitude, la.latitude, la.longitude), 0.01);
    }

    @Test
    public void distance() {
        assertEquals(distance, Geo.distance(seattle.latitude, seattle.longitude, la.latitude, la.longitude), 0.01);
    }

    @Test
    public void fastDistance() {
        // Allow 0.1% error
        assertEquals(distance, Geo.fastDistance(seattle.latitude, seattle.longitude, la.latitude, la.longitude), 0.001 * distance);
    }

    @Test
    public void moveBearing() {
        final LatLng moved = Geo.moveBearing(seattle.latitude, seattle.longitude, bearing, distance);
        assertEquals(la.latitude, moved.latitude, 0.01);
        assertEquals(la.longitude, moved.longitude, 0.01);
    }

}
