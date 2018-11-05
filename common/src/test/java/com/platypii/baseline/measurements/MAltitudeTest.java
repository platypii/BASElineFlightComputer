package com.platypii.baseline.measurements;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MAltitudeTest {

    private final MAltitude alt = new MAltitude(1541348400990L, 100.0, -2.0);

    @Test
    public void toRow() {
        // Shouldn't be serializing MAltitudes
        assertEquals("", alt.toRow());
    }

    @Test
    public void stringify() {
        assertEquals("MAltitude(1541348400990,100.0)", alt.toString());
    }

}
