package com.platypii.baseline.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Date utils
 */
public class Dates {

    /**
     * Format date as a short string for a chart axis
     */
    public static String chartDate(long millis) {
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return format.format(millis);
    }

}
