package com.platypii.baseline.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CSVHeaderTest {

    private final String firstLine = "date,double,long";

    @Test
    public void parseHeader() throws IOException {
        CSVHeader header = new CSVHeader(stringReader(firstLine));

        assertEquals(Integer.valueOf(0), header.get("date"));
        assertEquals(Integer.valueOf(1), header.get("double"));
        assertEquals(Integer.valueOf(2), header.get("long"));
        assertNull(header.get("nope"));
    }

    @Test
    public void parseHeaderWithMapping() throws IOException {
        CSVHeader header = new CSVHeader(stringReader(firstLine));
        header.addMapping("long", "okay");

        assertEquals(Integer.valueOf(0), header.get("date"));
        assertEquals(Integer.valueOf(1), header.get("double"));
        assertEquals(Integer.valueOf(2), header.get("long"));
        assertEquals(Integer.valueOf(2), header.get("okay"));
        assertNull(header.get("nope"));
    }

    @Test
    public void parseHeaderWithBOM() throws IOException {
        CSVHeader header = new CSVHeader(stringReader("\ufeffdate,double,long"));
        assertEquals(Integer.valueOf(0), header.get("date"));
    }

    private BufferedReader stringReader(String str) {
        return new BufferedReader(new StringReader(str));
    }

}
