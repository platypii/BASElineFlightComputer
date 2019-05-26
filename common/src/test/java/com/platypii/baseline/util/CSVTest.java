package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CSVTest {

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
    public void parseLine() {
        CSVHeader columns = new CSVHeader(firstLine);
        String[] row = "2018-11-04T16:20:00.99Z,3.14,1024".split(",");

        assertEquals(1541348400990L, CSVParse.getColumnDate(row, columns, "date"));
        assertEquals(3.14, CSVParse.getColumnDouble(row, columns, "double"), 0.0001);
        assertEquals(1024L, CSVParse.getColumnLong(row, columns, "long"));
    }

    @Test
    public void parseLineEmpty() {
        CSVHeader columns = new CSVHeader(firstLine);
        String[] row = ",,".split(",");

        assertEquals(-1L, CSVParse.getColumnDate(row, columns, "date"));
        assertEquals(Double.NaN, CSVParse.getColumnDouble(row, columns, "double"), 0.0001);
        assertEquals(-1L, CSVParse.getColumnLong(row, columns, "long"));
    }

    @Test
    public void parseLineBad() {
        CSVHeader columns = new CSVHeader(firstLine);
        String[] row = firstLine.split(",");

        assertEquals(-1L, CSVParse.getColumnDate(row, columns, "date"));
        assertEquals(Double.NaN, CSVParse.getColumnDouble(row, columns, "double"), 0.0001);
        assertEquals(-1L, CSVParse.getColumnLong(row, columns, "long"));
    }

}
