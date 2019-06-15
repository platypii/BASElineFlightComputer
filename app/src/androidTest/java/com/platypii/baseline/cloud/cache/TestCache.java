package com.platypii.baseline.cloud.cache;

import androidx.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

class TestCache extends LocalCache<String> {
    TestCache() {
        super("cloud.cache.test");
    }

    @NonNull
    @Override
    Type listType() {
        return new TypeToken<List<String>>(){}.getType();
    }

    @NonNull
    @Override
    String getId(@NonNull String item) {
        return item;
    }
}
