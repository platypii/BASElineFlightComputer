package com.platypii.baseline.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are IOing correctly
 */
public class IOUtilTest {

    @Test
    public void copy() throws IOException {
        InputStream is = new ByteArrayInputStream("BASE".getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtil.copy(is, os);
        assertEquals("BASE", os.toString());
    }

    @Test
    public void streamToString() throws IOException {
        InputStream is = new ByteArrayInputStream("BASE".getBytes());
        String out = IOUtil.toString(is);
        assertEquals("BASE", out);
    }

}
