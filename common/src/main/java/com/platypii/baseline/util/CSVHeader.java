package com.platypii.baseline.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CSVHeader {

    private final Map<String, Integer> columns = new HashMap<>();

    public CSVHeader(@Nullable String line) {
        if (line != null) {
            final String[] header = line.split(",");
            for (int i = 0; i < header.length; i++) {
                columns.put(header[i], i);
            }
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
