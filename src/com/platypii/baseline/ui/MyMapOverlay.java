package com.platypii.baseline.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.platypii.baseline.audible.MyFlightManager;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.R;


public class MyMapOverlay extends Overlay {

    // Re-usable structures
    private Paint paint = new Paint();
    private Paint text = new Paint();
    private Paint outline = new Paint();
    private Path path = new Path();
    private static Path hand = new Path();
    static {
    	final float scale = 6;
        final float w1 = 5; // Width of the arrow
        final float w2 = 2; // Depth of the notch
        hand.moveTo(-w1 * scale,  10 * scale);
        hand.lineTo(          0, -10 * scale);
        hand.lineTo( w1 * scale,  10 * scale);
        hand.lineTo(          0,  (10 - w2) * scale);
    }
    private Bitmap pin;
    
    private float density = 1;

    
    public MyMapOverlay(Context context) {
        super();
        density = context.getResources().getDisplayMetrics().density;
        paint.setAntiAlias(true);
        text.setAntiAlias(true);
        text.setColor(0xff111111);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        outline.setAntiAlias(true);
        outline.setColor(0xffeeeeee);
        outline.setStyle(Paint.Style.STROKE);
        outline.setTypeface(Typeface.DEFAULT_BOLD);
        pin = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pin);
    }
    
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if(!shadow) {
            // Projection from coordinates to screen pixels
            Projection proj = mapView.getProjection();

            // Draw the track
            drawTrack(canvas, proj);

            // Draw path to home
            drawHomePath(canvas, proj);
            
            // TODO: Draw GPS
            
            // Draw my location
            drawMyLocation(canvas, proj);
            
            // Draw home location
            drawHomeLocation(canvas, proj);
            
            // Draw the scale bar
            drawScaleBar(canvas, mapView);
            
            // Draw linear altimeter?
            // linearAltimeter.drawLinearAltimeter(canvas, density, true);
        }
    }
    
    /**
     * Draws the GPS track
     */
    private void drawTrack(Canvas canvas, Projection proj) {
        boolean empty = true;
        synchronized(path) {
            path.rewind();
            Point pt = new Point();
            synchronized(MyLocationManager.history) {
	            for(MyLocation loc : MyLocationManager.history) {
	                GeoPoint gp = Convert.locToGeoPoint(loc.latitude, loc.longitude);
	                proj.toPixels(gp, pt);
	            	if(empty) {
	                	path.moveTo(pt.x, pt.y);
	                	empty = false;
	            	} else {
	                	path.lineTo(pt.x, pt.y);
	            	}
	            }
            }
            paint.setColor(0xaadd0000);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5 * density);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawPath(path, paint);
        }
    }
    
    /**
     * Draws the path to the home location
     */
    private void drawHomePath(Canvas canvas, Projection proj) {
        if(MyFlightManager.homeLoc != null) {
            // Home location to screen pixels
            GeoPoint homeGeoPoint = Convert.locToGeoPoint(MyFlightManager.homeLoc);
            Point homePoint = proj.toPixels(homeGeoPoint, null);

            // Draw path to home
            if(MyLocationManager.lastLoc != null) {
            	// Current location to screen pixels
	            GeoPoint currentGeoPoint = Convert.locToGeoPoint(MyLocationManager.latitude, MyLocationManager.longitude);
	            Point currentPoint = proj.toPixels(currentGeoPoint, null);
	        	paint.setColor(0xffeeeeee);
	        	paint.setStyle(Paint.Style.STROKE);
	            paint.setStrokeWidth(3 * density);
	            paint.setStrokeCap(Paint.Cap.ROUND);
            	canvas.drawLine(currentPoint.x, currentPoint.y, homePoint.x, homePoint.y, paint);
            }
        }
    }

    /**
     * Draws the current location and bearing indicator
     */
    private void drawMyLocation(Canvas canvas, Projection proj) {
        if(MyLocationManager.lastLoc != null) {
        	// Current location to screen pixels
            GeoPoint currentGeoPoint = Convert.locToGeoPoint(MyLocationManager.latitude, MyLocationManager.longitude);
            Point currentPoint = proj.toPixels(currentGeoPoint, null);
		    paint.setColor(0xffaa1111);
		    paint.setStyle(Paint.Style.FILL);
	        canvas.save();
	        canvas.translate(currentPoint.x, currentPoint.y);
		    if(!Double.isNaN(MyLocationManager.bearing))
		        canvas.rotate((float)(MyLocationManager.bearing), 0, 0);
	        canvas.drawPath(hand, paint);
	        canvas.restore();
	    }
    }

    /**
     * Draws the home location
     */
    private void drawHomeLocation(Canvas canvas, Projection proj) {
        if(MyFlightManager.homeLoc != null) {
            // Home location to screen pixels
            GeoPoint homeGeoPoint = Convert.locToGeoPoint(MyFlightManager.homeLoc);
            Point homePoint = proj.toPixels(homeGeoPoint, null);

            // Draw the home icon
        	// canvas.drawCircle(pt2.x, pt2.y, 5, paint);
            canvas.drawBitmap(pin, homePoint.x - 16*density, homePoint.y - 30*density, paint);
        }
    }
    
    /**
     * Draws a scale bar
     */
    private void drawScaleBar(Canvas canvas, MapView mapView) {
        final int start = (int) (6 * density);
        final int end = (int) (106 * density);
        final int center_y = (int) (6 * density);
        final int height_y = (int) (14 * density);
        
        // Find scale bar distance
        double latitude = mapView.getMapCenter().getLatitudeE6() * 1E-6;  
        double ppm = (mapView.getProjection().metersToEquatorPixels(1) / (Math.cos(Math.toRadians(latitude)))); // pixels per meter
        double screen_dist = end - start;
        double earth_dist = screen_dist / ppm;
    /*
        Point pt1 = new Point(start, center_y);
        Point pt2 = new Point(end, center_y);
        Projection projection = mapView.getProjection();
        GeoPoint gp1 = projection.fromPixels(pt1.x, pt1.y);
        GeoPoint gp2 = projection.fromPixels(pt2.x, pt2.y);
        double lat1 =  gp1.getLatitudeE6() * 1E-6;
        double long1 = gp1.getLongitudeE6() * 1E-6;
        double lat2 =  gp2.getLatitudeE6() * 1E-6;
        double long2 = gp2.getLongitudeE6() * 1E-6;
        float results[] = new float[1];
        Location.distanceBetween(lat1, long1, lat2, long2, results);
        double earth_dist1 = results[0];
    */
                    
        // Draw scale
        path.rewind();
        path.setLastPoint(start, center_y + height_y);
        path.lineTo(start, center_y);
        path.lineTo(end, center_y);
        path.lineTo(end, center_y + height_y);
        
        paint.setStyle(Style.STROKE);
        paint.setStrokeCap(Cap.SQUARE);
        paint.setStrokeJoin(Join.MITER);
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(4 * density);
        canvas.drawPath(path, paint);
        paint.setColor(0xff000000);
        paint.setStrokeWidth(2 * density);
        canvas.drawPath(path, paint);
        
        // Draw label
        text.setTextSize(14 * density);
        outline.setStrokeWidth(1.5f * density);
        outline.setTextSize(14 * density);
        canvas.drawText(Convert.distance(earth_dist), start + 4 * density, center_y + 14 * density, outline);
        canvas.drawText(Convert.distance(earth_dist), start + 4 * density, center_y + 14 * density, text);
        
    }
}

