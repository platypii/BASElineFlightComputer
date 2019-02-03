package com.platypii.baseline.views.charts.layers;

import androidx.annotation.ColorInt;

public class Colors {

    @ColorInt
    public static final int defaultColor = 0xff7f00ff; // Default track color

    private static final int[] colors = {
            0xff6f00ff,
            0xffee1111,
            0xff11ee11,
            0xff1111ee,
            0xff11eeee,
            0xffeeee11
    };

    private static int nextIndex = 0;

    @ColorInt
    public static int nextColor() {
        return colors[nextIndex++ % colors.length];
    }

}
