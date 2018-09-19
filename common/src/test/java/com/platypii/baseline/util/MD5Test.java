package com.platypii.baseline.util;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are mathing correctly
 */
public class MD5Test {

    @Test
    public void emptyFile() throws IOException {
        File tempfile = File.createTempFile("md5-test-", ".txt");
        String checksum = MD5.md5(tempfile);
        tempfile.delete();

        assertEquals("d41d8cd98f00b204e9800998ecf8427e", checksum);
    }

}
