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
    public static final int starredTracks = 0x706b00ff;

    @ColorInt
    private static final int[] colors = {
            0xffcc1122,
            0xff11aa22,
            0xff1866b4,
            0xffdd77dd,
            0xff11bbcc,
            0xffbbbb11,
            0xffbbbbbb,
            0xffd74d09,
            0xff7f00ff,
    };

    private static int nextIndex = 0;

    @ColorInt
    public static int nextColor() {
        return colors[nextIndex++ % colors.length];
    }

}
