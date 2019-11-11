package com.platypii.baseline.views.charts.layers;

import androidx.annotation.ColorInt;

public class Colors {

    @ColorInt
    public static final int defaultColor = 0xff7f00ff; // Default track color

    @ColorInt
    public static final int modeGround = 0xff995522;
    @ColorInt
    public static final int modePlane = 0xffdd2222;
    @ColorInt
    public static final int modeFreefall = 0xff1111ee;
    @ColorInt
    public static final int modeWingsuit = 0xff6b00ff;
    @ColorInt
    public static final int modeCanopy = 0xff11dd11;

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
