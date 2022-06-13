package com.platypii.baseline.util.cache;

import androidx.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import com.platypii.baseline.util.LocalCache;
import java.lang.reflect.Type;
import java.util.List;

class TestCache extends LocalCache<String> {
    TestCache() {
        super("cloud.cache.test");
    }

    @NonNull
    @Override
    public Type listType() {
        return new TypeToken<List<String>>(){}.getType();
    }

    @NonNull
    @Override
    public String getId(@NonNull String item) {
        return item;
    }
}
