package com.platypii.baseline.measurements;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MPressureTest {

    private final MPressure pres = new MPressure(1541348400990L, 111000L, 100.0, -2.0, 1014.3f);

    @Test
    public void toRow() {
        assertEquals("1541348400990,111000,alt,1014.3", pres.toRow());
    }

    @Test
    public void stringify() {
        assertEquals("MPressure(1541348400990,1014.30)", pres.toString());
    }

}
