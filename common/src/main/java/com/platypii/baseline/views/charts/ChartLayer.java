package com.platypii.baseline.views.charts;

import android.graphics.Paint;
import android.support.annotation.NonNull;

public interface ChartLayer {
    void drawData(@NonNull Plot plot, Paint paint, Paint text);
}
