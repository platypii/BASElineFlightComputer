package com.platypii.baseline.views.charts.layers;

import androidx.annotation.ColorInt;

public class Colors {

    @ColorInt
    public static final int defaultColor = 0xff7f00ff; // Default track color

    @ColorInt
    private static final int[] colors = {
            0xff6f00ff,
            0xffdd1111,
            0xff11cc11,
            0xff2222ee,
            0xffdd77dd,
            0xff11bbdd,
            0xffcccc11,
            0xffbbbbbb
    };

    private static int nextIndex = 0;

    @ColorInt
    public static int nextColor() {
        return colors[nextIndex++ % colors.length];
    }

}
