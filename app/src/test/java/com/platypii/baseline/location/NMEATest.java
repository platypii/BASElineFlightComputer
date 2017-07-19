package com.platypii.baseline.location;

import android.support.annotation.NonNull;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Ensure that we are parsing NMEA correctly
 */
public class NMEATest {

    @Test
    public void nmeaValidate() {
        assertTrue(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72"));
        assertFalse(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*71"));
        assertFalse(validate("GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72"));
        assertFalse(validate("$GPATT,***"));
        assertFalse(validate("$GPATT*"));
    }

    @Test
    public void nmeaChecksum() {
        assertTrue(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72"));
        assertFalse(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*99"));
    }

    @Test
    public void pglorChecksum() {
        // PGLOR truncates the checksum, but we let it slide:
        assertTrue(validate("$PGLOR,1,SAT,G07,011,0,G30,014,13,G09,016,1F,G05,009,0,G27,017,0,G28,017,3F,G08,021,3F,G16,020,1F,G13,015,33,G23,019,3*3"));
        assertTrue(validate("$PGLOR,1,SAT,G29,025,1F,G02,023,1F,R20,013,37,G12,011,0,G31,021,1F,G05,015,1F,G21,011,0,G20,011,0,G26,015,3F,G23,011,0*5"));
    }

    private static boolean validate(@NonNull String nmea) {
        try {
            NMEA.validate(nmea);
            return true;
        } catch (NMEAException e) {
            return false;
        }
    }
}
