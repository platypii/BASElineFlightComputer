package com.platypii.baseline.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CSVHeader {

    private final Map<String, Integer> columns = new HashMap<>();

    public CSVHeader(@NonNull String line) {
        final String[] header = line.split(",");
        for (int i = 0; i < header.length; i++) {
            columns.put(header[i], i);
        }
    }

    @Nullable
    public Integer get(@NonNull String columnName) {
        return columns.get(columnName);
    }

    public void addMapping(@NonNull String from, @NonNull String to) {
        if (columns.containsKey(from) && !columns.containsKey(to)) {
            columns.put(to, columns.get(from));
        }
    }

}
