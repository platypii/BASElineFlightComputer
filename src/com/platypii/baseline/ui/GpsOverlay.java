package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationManager;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class GpsOverlay extends Overlay {
	
	private Paint paint = new Paint();
	
	
	public GpsOverlay() {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(4);
	}

    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if(!shadow) {
            Projection proj = mapView.getProjection();

            // Draw last gps location
            MyLocation loc = MyLocationManager.lastLoc;
            drawLocation(canvas, loc, proj, paint, true);
        }
    }
    
    private void drawLocation(Canvas canvas, MyLocation loc, Projection proj, Paint paint, boolean border) {
        if(loc != null) {
            GeoPoint gp = Convert.locToGeoPoint(loc.latitude, loc.longitude);
            Point pt = proj.toPixels(gp, null);
            
            float radius = (float)(proj.metersToEquatorPixels(loc.hdop) / (Math.cos(Math.toRadians(loc.latitude))));
        
            long time = System.currentTimeMillis() - loc.timeMillis;
            time = Math.max(0, time);
//          int alpha = (int)(140000.0 / (time + 500)) - 10;
            int alpha = (int)(100 - time / 50);
            alpha = Math.max(0, Math.min(100, alpha));
        
            if(border)
                alpha += 80;
            
            paint.setColor(0x662255bb);
            paint.setAlpha(alpha);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(pt.x, pt.y, radius, paint);
            
            if(border) {
                paint.setColor(0xaa0000ee);
                paint.setAlpha(Math.min(255, alpha + 60));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(pt.x, pt.y, radius, paint);
            }
        }
    }
    
}

