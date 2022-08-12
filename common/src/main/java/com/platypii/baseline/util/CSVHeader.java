package com.platypii.baseline.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CSVHeader {

    private static final String flysight2Header = "$FLYS,1";

    @NonNull
    private final Map<String, Integer> columns = new HashMap<>();

    public CSVHeader(@NonNull BufferedReader br) throws IOException {
        final String[] header = getHeaderLine(br).split(",");
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

    /** Get the column header names */
    @NonNull
    private String getHeaderLine(@NonNull BufferedReader br) throws IOException {
        String firstLine = br.readLine();
        if (firstLine == null) {
            return "";
        }
        firstLine = firstLine.replace("\ufeff", ""); // Remove BOM
        if (flysight2Header.equals(firstLine)) {
            // FlySight2 parsing
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("$COL,GNSS,")) {
                    return line.replace("$COL,", "");
                }
            }
            return "";
        } else {
            return firstLine;
        }
    }

}
