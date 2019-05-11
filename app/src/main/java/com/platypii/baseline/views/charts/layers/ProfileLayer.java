package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;
import android.graphics.Paint;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class ProfileLayer extends ChartLayer {

    private static final int AXIS_PROFILE = 0;
    protected final DataSeries profileSeries = new DataSeries();
    @NonNull
    public String name = "";
    @ColorInt
    public final int color;

    ProfileLayer(@ColorInt int color) {
        this.color = color;
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (profileSeries.size() == 0) {
            plot.text.setTextAlign(Paint.Align.CENTER);
            plot.canvas.drawText("no track data", plot.width / 2, plot.height / 2, plot.text);
        } else {
            // Draw data
            plot.paint.setColor(0xff7f00ff);
            plot.drawLine(AXIS_PROFILE, profileSeries, 1.5f);
        }
    }

}
