package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.views.charts.Plot;
import android.graphics.BlurMaskFilter;
import android.graphics.Paint;
import android.os.Build;
import androidx.annotation.NonNull;

public class EllipseLayer extends ChartLayer {

    @NonNull
    private final BlurMaskFilter blurry;

    public EllipseLayer(float density) {
        blurry = new BlurMaskFilter(2 * density, BlurMaskFilter.Blur.NORMAL);
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // minsdk 21
            plot.paint.setStyle(Paint.Style.FILL);
            plot.paint.setMaskFilter(blurry);
            // Draw canopy ellipse
            plot.paint.setColor(0x3344ee44);
            plot.canvas.save();
            plot.canvas.rotate(9, plot.getX(11), plot.getY(-5.5));
            plot.canvas.drawOval(plot.getX(1), plot.getY(-1), plot.getX(21), plot.getY(-10), plot.paint);
            plot.canvas.restore();
            // Draw wingsuit ellipse
            plot.paint.setColor(0x339e62f2);
            plot.canvas.save();
            plot.canvas.rotate(35, plot.getX(38), plot.getY(-21));
            plot.canvas.drawOval(plot.getX(20), plot.getY(-10), plot.getX(56), plot.getY(-32), plot.paint);
            plot.canvas.restore();
            plot.paint.setMaskFilter(null);
        }
    }

}
