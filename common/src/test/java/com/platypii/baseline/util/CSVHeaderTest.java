package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CSVHeaderTest {

    private final String firstLine = "date,double,long";

    @Test
    public void parseHeader() {
        CSVHeader header = new CSVHeader(firstLine);

        assertEquals(Integer.valueOf(0), header.get("date"));
        assertEquals(Integer.valueOf(1), header.get("double"));
        assertEquals(Integer.valueOf(2), header.get("long"));
        assertNull(header.get("nope"));
    }

    @Test
    public void parseHeaderWithMapping() {
        CSVHeader header = new CSVHeader(firstLine);
        header.addMapping("long", "okay");

        assertEquals(Integer.valueOf(0), header.get("date"));
        assertEquals(Integer.valueOf(1), header.get("double"));
        assertEquals(Integer.valueOf(2), header.get("long"));
        assertEquals(Integer.valueOf(2), header.get("okay"));
        assertNull(header.get("nope"));
    }

    @Test
    public void parseHeaderWithBOM() {
        CSVHeader header = new CSVHeader("\ufeffdate,double,long");
        assertEquals(Integer.valueOf(0), header.get("date"));
    }

}
