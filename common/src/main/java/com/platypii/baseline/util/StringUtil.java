package com.platypii.baseline.util;

import androidx.annotation.NonNull;

public class StringUtil {

    /**
     * Return the index of the start of a line
     */
    public static int lineStartIndex(@NonNull CharSequence str, int lineNumber) {
        final int n = str.length();
        int lineCount = 1;
        for (int i = 0; i < n; i++) {
            if (lineCount == lineNumber) {
                return i;
            }
            if (str.charAt(i) == '\n') {
                lineCount++;
            }
        }
        return n;
    }
}
