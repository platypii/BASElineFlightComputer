package com.platypii.baseline.ui;

import com.platypii.baseline.audible.MyFlightManager;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.SweepGradient;
import android.graphics.BlurMaskFilter.Blur;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;


public class RadarMap extends View {
    
    // Avoid creating new objects unnecessarily
	private Point pt = new Point();
	private Point homePoint = new Point();
    private Paint paint = new Paint();
    private BlurMaskFilter blurMask;
    private Path clip = new Path();
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
    private final static int sweep[] = new int[]{0xff001100, 0xff002211, 0xff005511}; // SweepGradient colors
    private SweepGradient gradient;
    private Bitmap pin;
    
    // Only valid during execution of onDraw()
    private float center_x;
    private float center_y;
    private float radius;
    private double theta; // The map rotation (0 = north up, bearing, compass)
    
    // Draw options
    private static final float VIEW_DISTANCE = 200; // The view radius in meters

    
    public RadarMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        final float density = getResources().getDisplayMetrics().density;
        blurMask = new BlurMaskFilter(8 * density, Blur.INNER);
        paint.setAntiAlias(true);
        pin = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pin);

        // Start GPS updates
        MyLocationManager.addListener(new MyLocationListener() {    
			public void onLocationChanged(MyLocation loc) {
                postInvalidate();
			}
        });
        
    }
    
    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        center_x = width / 2.0f;
        center_y = height / 2.0f;
        radius = Math.min(center_x, center_y) - 5;
        
        clip.rewind();
        clip.addCircle(center_x, center_y, radius, Path.Direction.CW);
        gradient = new SweepGradient(center_x, center_y, sweep, null);
    }
    	
	@Override
    protected void onDraw(Canvas canvas) {
        final float density = getResources().getDisplayMetrics().density;
        theta = Double.isNaN(MyLocationManager.bearing)? 0 : -MyLocationManager.bearing;

        // Clipping
        canvas.save();
        canvas.clipPath(clip);

        // Draw radar face
        paint.setColor(0xff002210);
        paint.setStyle(Paint.Style.FILL);
        // paint.setShader(new SweepGradient(center_x, center_y, 0xff002211, 0xff007711));
        paint.setShader(gradient);
        paint.setMaskFilter(blurMask); // inset
        canvas.drawCircle(center_x, center_y, radius, paint);
        paint.setMaskFilter(null);
        paint.setShader(null);

        // Inner lines
        paint.setColor(0xdd009922);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1 * density);
        canvas.drawCircle(center_x, center_y, radius / 3, paint);
        canvas.drawCircle(center_x, center_y, radius * 2 / 3, paint);
        // Crosshairs
        canvas.drawLine(center_x - radius, center_y, center_x + radius, center_y, paint);
        canvas.drawLine(center_x, center_y - radius, center_x, center_y + radius, paint);

        // Draw the track
        paint.setColor(0xff771111);
        paint.setStrokeWidth(4 * density);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeJoin(Join.ROUND);
        synchronized(path) {
            path.rewind();
            boolean empty = true;
            synchronized(MyLocationManager.history) {
	            for(MyLocation loc : MyLocationManager.history) {
	            	locToPoint(loc.loc(), pt);
	            	if(empty) {
	                	path.moveTo(pt.x, pt.y);
	                	empty = false;
	            	} else {
	                	path.lineTo(pt.x, pt.y);
	            	}
	            }
            }
            canvas.drawPath(path, paint);
        }
        
        // Draw path to home
        if(MyLocationManager.lastLoc != null && MyFlightManager.homeLoc != null) {
            // Home location to screen pixels
        	locToPoint(MyFlightManager.homeLoc, homePoint);

            // Draw path to home
            paint.setColor(0xffeeeeee);
            paint.setStrokeWidth(2 * density);
        	canvas.drawLine(center_x, center_y, homePoint.x, homePoint.y, paint);
        }

        // My Location
        if(MyLocationManager.lastLoc != null) {
	        paint.setColor(0xff991111);
	        paint.setStyle(Paint.Style.FILL);
	        canvas.save();
	        canvas.translate(center_x, center_y);
	        canvas.drawPath(hand, paint);
	        canvas.restore();
        }

        // Clear clipping
        canvas.restore();

        // Draw home icon
        if(MyLocationManager.lastLoc != null && MyFlightManager.homeLoc != null) {
            // Home location to screen pixels
            locToPoint(MyFlightManager.homeLoc, homePoint);

            // Draw the home icon
        	// canvas.drawCircle(pt2.x, pt2.y, 5, paint);
            canvas.drawBitmap(pin, homePoint.x - 16*density, homePoint.y - 30*density, paint);
        }

    }

    /**
     * Maps a Location to screen space
     */
	private Point locToPoint(Location loc, Point out) {
		if(MyLocationManager.lastLoc != null) {
			Location currLoc = MyLocationManager.lastLoc.loc();
			float dist = currLoc.distanceTo(loc);
			float bearing = currLoc.bearingTo(loc);
			// float r = radius * dist / VIEW_DISTANCE;
			// float r = radius * (1 - 1 / (dist / VIEW_DISTANCE + 1));
			float r = (float) (radius * (1 - Math.pow(2, -dist / VIEW_DISTANCE)));
			int x = (int) (center_x + r * Math.sin(Math.toRadians(bearing + theta)));
			int y = (int) (center_y - r * Math.cos(Math.toRadians(bearing + theta)));
			if(out != null) {
				out.set(x, y);
				return out;
			} else {
				return new Point(x, y);
			}
		} else {
			return null;
		}
	}

}


