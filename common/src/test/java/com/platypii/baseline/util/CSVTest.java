package com.platypii.baseline.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CSVTest {

    private final String firstLine = "date,double,long";

    private final CSVHeader columns = new CSVHeader(new BufferedReader(new StringReader(firstLine)));

    public CSVTest() throws IOException {
    }

    @Test
    public void parseLine() {
        String[] row = "2018-11-04T16:20:00.99Z,3.14,1024".split(",");

        assertEquals(1541348400990L, CSVParse.getColumnDate(row, columns, "date"));
        assertEquals(3.14, CSVParse.getColumnDouble(row, columns, "double"), 0.0001);
        assertEquals(1024L, CSVParse.getColumnLong(row, columns, "long"));
    }

    @Test
    public void parseLineEmpty() {
        String[] row = ",,".split(",");

        assertEquals(-1L, CSVParse.getColumnDate(row, columns, "date"));
        assertEquals(Double.NaN, CSVParse.getColumnDouble(row, columns, "double"), 0.0001);
        assertEquals(-1L, CSVParse.getColumnLong(row, columns, "long"));
    }

    @Test
    public void parseLineBad() {
        String[] row = firstLine.split(",");

        assertEquals(-1L, CSVParse.getColumnDate(row, columns, "date"));
        assertEquals(Double.NaN, CSVParse.getColumnDouble(row, columns, "double"), 0.0001);
        assertEquals(-1L, CSVParse.getColumnLong(row, columns, "long"));
    }

}
