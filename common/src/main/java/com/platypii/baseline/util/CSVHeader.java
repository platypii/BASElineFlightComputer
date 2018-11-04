package com.platypii.baseline.util;

import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CSVHeader {

    private Map<String, Integer> columns = new HashMap<>();

    public CSVHeader(String line) {
        final String[] header = line.split(",");
        for (int i = 0; i < header.length; i++) {
            columns.put(header[i], i);
        }
    }

    @Nullable
    public Integer get(String columnName) {
        return columns.get(columnName);
    }

    public void addMapping(String from, String to) {
        if (columns.containsKey(from) && !columns.containsKey(to)) {
            columns.put(to, columns.get(from));
        }
    }

}
