package com.platypii.baseline.location;

import org.junit.Test;

import static com.platypii.baseline.location.NMEA.validate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Ensure that we are parsing NMEA correctly
 */
public class NMEATest {

    @Test
    public void parseDegreesMinutes() {
        // Parse DDDMM.MMMM,N into decimal degrees
        assertEquals(123.761315, NMEA.parseDegreesMinutes("12345.6789", "N"), 0.01);
        assertEquals(-123.761315, NMEA.parseDegreesMinutes("12345.6789", "S"), 0.01);
        assertEquals(Double.NaN, NMEA.parseDegreesMinutes("", ""), 0.01);
        assertEquals(Double.NaN, NMEA.parseDegreesMinutes("X", ""), 0.01);
        assertEquals(Double.NaN, NMEA.parseDegreesMinutes("XX.YY", ""), 0.01);
        assertEquals(Double.NaN, NMEA.parseDegreesMinutes("12345", ""), 0.01);
    }

    @Test
    public void parseDate() {
        // Parse DDMMYY into milliseconds since epoch
        assertEquals(1524182400000L, NMEA.parseDate("200418"));
        assertEquals(0L, NMEA.parseDate(null));
        assertEquals(0L, NMEA.parseDate(""));
        assertEquals(0L, NMEA.parseDate("X"));
    }

    @Test
    public void parseTime() {
        // Parse HHMMSS.SS UTC time into milliseconds since midnight
        assertEquals(72258990L, NMEA.parseTime("200418.99"));
        assertEquals(0L, NMEA.parseTime(null));
        assertEquals(0L, NMEA.parseTime(""));
        assertEquals(0L, NMEA.parseTime("X"));
    }

    @Test
    public void nmeaValidate() {
        assertTrue(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72"));
        assertFalse(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*71"));
        assertFalse(validate("GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72"));
        assertFalse(validate("$GPATT,***"));
        assertFalse(validate("$GPATT*"));
        assertFalse(validate("$AIDSTAT,1,2,3*00")); // AIDSTAT always has 00
    }

    @Test
    public void nmeaChecksum() {
        assertTrue(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72"));
        assertFalse(validate("$GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*99"));
    }

    @Test
    public void parsePower() {
        // Dual proprietary sentence for battery level
        String notCharging = "$GPPWR,04C3,0,0,0,0,00,0,0,97, 1 9 ,S00";
        assertEquals(0.677, NMEA.parsePowerLevel(NMEA.splitNmea(notCharging)), 0.001);
        assertEquals(Double.NaN, NMEA.parsePowerLevel(NMEA.splitNmea("")), 0.001);
    }

    @Test
    public void cleanNmea() {
        assertEquals("$GPFOO,0,1,2,3*99", NMEA.cleanNmea("$GPFOO,0,1,2,3*99"));
        assertEquals("$GPFOO,0,1,2,3*99", NMEA.cleanNmea("$GPFOO,0,1,2,3*99\n"));
        assertEquals("$GPFOO,0,1,2,3*99", NMEA.cleanNmea("$GPFOO,0,1,2,3*99\0"));
        assertEquals("$GPFOO,0,1,2,3*99", NMEA.cleanNmea("$GPFOO,0,1,2,3*99 "));
        assertEquals("$GPFOO,0,1,2,3*99", NMEA.cleanNmea(" $GPFOO,0,1,2,3*99 "));
        assertEquals("$GPFOO,0,1,2,3*99", NMEA.cleanNmea("\0$GPFOO,0,1,2,3*99 "));
    }

    @Test
    public void splitNmea() {
        String[] split = {"$GPFOO", "0", "1", "2", "3"};
        assertArrayEquals(split, NMEA.splitNmea("$GPFOO,0,1,2,3*99"));
    }

}
