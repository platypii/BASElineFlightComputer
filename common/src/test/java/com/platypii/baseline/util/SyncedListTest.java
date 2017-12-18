package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are mathing correctly
 */
public class SyncedListTest {

    @Test
    public void create() {
        SyncedList<String> list = new SyncedList<>();
        list.setMaxSize(2);
        list.append("foo");
        list.append("bar");
        list.append("baz");

        assertEquals(2, list.size());
        assertEquals("SyncedList(bar, baz)", list.toString());
    }

    @Test
    public void string() {
        SyncedList<String> list = new SyncedList<>();
        list.setMaxSize(2);

        assertEquals("SyncedList()", list.toString());
        list.append("foo");
        assertEquals("SyncedList(foo)", list.toString());
        list.append("bar");
        assertEquals("SyncedList(foo, bar)", list.toString());
    }

}
