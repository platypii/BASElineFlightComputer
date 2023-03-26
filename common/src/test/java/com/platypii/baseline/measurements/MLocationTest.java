package com.platypii.baseline.measurements;

import com.google.android.gms.maps.model.LatLng;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MLocationTest {

    private final MLocation seattle = new MLocation(1541348400990L, 47.60, -122.33, 100.0, -2.0, 3.0, 4.0, 0f, 0f, 0f, 0f, 0, 0);
    private final LatLng la = new LatLng(34.0, -118.2);

    @Test
    public void dummy() {
        MLocation dummy = new MLocation();
        assertEquals(Double.NaN, dummy.latitude, 0.01);
        assertEquals(Double.NaN, dummy.longitude, 0.01);
    }

    @Test
    public void speeds() {
        assertEquals(5.00, seattle.groundSpeed(), 0.01);
        assertEquals(5.39, seattle.totalSpeed(), 0.01);
    }

    @Test
    public void glides() {
        assertEquals(-21.80, seattle.glideAngle(), 0.01);
        assertEquals(2.50, seattle.glideRatio(), 0.01);
    }

    @Test
    public void bearings() {
        assertEquals(165.66, seattle.bearingTo(la), 0.01);
    }

    @Test
    public void distances() {
        assertEquals(1551093.52, seattle.distanceTo(la), 0.01);
        assertEquals(0, seattle.distanceTo(seattle), 0.01);
        assertEquals(0, seattle.distanceTo(seattle.latLng()), 0.01);
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void comparison() {
        assertTrue(seattle.equals(seattle));
        assertEquals(0, seattle.compareTo(seattle));
    }

    @Test
    public void toRow() {
        assertEquals("1541348400990,,gps,,47.6,-122.33,100,3,4,0", seattle.toRow());
    }

    @Test
    public void stringify() {
        assertEquals("MLocation(2018-11-04T16:20:00.990Z,47.600000,-122.330000,100.0,3,4)", seattle.toString());
    }

}
