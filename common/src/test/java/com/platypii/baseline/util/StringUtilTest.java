package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are Stringing correctly
 */
public class StringUtilTest {

    /**
     * Check that StringUtil.lineStartIndex works
     */
    @Test
    public void lineStartIndex() {
        assertEquals(0, StringUtil.lineStartIndex("", 1));
        assertEquals(0, StringUtil.lineStartIndex("", 10));
        assertEquals(0, StringUtil.lineStartIndex("Yup", 1));
        assertEquals(0, StringUtil.lineStartIndex("Yup\nYup\n", 1));
        assertEquals(4, StringUtil.lineStartIndex("Yup\nYup\n", 2));
        assertEquals(8, StringUtil.lineStartIndex("Yup\nYup\n", 3));
        assertEquals(0, StringUtil.lineStartIndex("\nYup\nYup\n", 1));
        assertEquals(0, StringUtil.lineStartIndex("\n\nYup\nYup\n", 1));
        assertEquals(1, StringUtil.lineStartIndex("\n\nYup\nYup\n", 2));
        assertEquals(2, StringUtil.lineStartIndex("\n\nYup\nYup\n", 3));
        assertEquals(6, StringUtil.lineStartIndex("\n\nYup\nYup\n", 4));
        assertEquals(10, StringUtil.lineStartIndex("\n\nYup\nYup\n", 5));
        assertEquals(10, StringUtil.lineStartIndex("\n\nYup\nYup\n", 40));
    }

}
