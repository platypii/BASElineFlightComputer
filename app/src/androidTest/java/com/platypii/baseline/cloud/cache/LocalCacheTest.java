package com.platypii.baseline.cloud.cache;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalCacheTest {

    @Test
    public void cacheStuff() {
        Context context = ApplicationProvider.getApplicationContext();
        TestCache cache = new TestCache();
        cache.start(context);

        cache.clear();
        assertNull(cache.list());

        cache.update(new ArrayList<>());
        assertEquals(0, cache.list().size());

        cache.add("bun");
        assertEquals(1, cache.list().size());
        assertEquals("bun", cache.get("bun"));

        cache.remove("bun");
        assertEquals(0, cache.list().size());

        cache.clear();
        assertNull(cache.list());
    }

}
