package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.views.charts.layers.ChartLayer;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class FlightProfileTouchable extends FlightProfile {

    private float scaleFactor = 1;

    final List<TrackData> tracks = new ArrayList<>();
    final List<LaserProfile> lasers = new ArrayList<>();

    public FlightProfileTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final ScaleGestureDetector scaler = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(1f, Math.min(scaleFactor, 10f));
            invalidate();
            return true;
        }
    });

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        scaler.onTouchEvent(event);

        // Focus
        if (!scaler.isInProgress()) {
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_MOVE) {
                final double x = plot.getXinverse(0, event.getX());
                final double y = plot.getYinverse(0, event.getY());
                // Find nearest data point, and emit chart focus event
                EventBus.getDefault().post(findClosest(x, y));
            } else if (action == MotionEvent.ACTION_UP) {
                // Clear chart focus event
                EventBus.getDefault().post(new ChartFocusEvent.Unfocused());
            }
        }
        return true; // event was handled
    }

    @Override
    public void addLayer(@NonNull ChartLayer layer) {
        super.addLayer(layer);
        // Keep focus layer on top
        removeLayer(focusLayer);
        super.addLayer(focusLayer);
        // Add to track/laser lists for focusing later
        if (layer instanceof TrackProfileLayer) {
            tracks.add(((TrackProfileLayer) layer).trackData);
        } else if (layer instanceof LaserProfileLayer) {
            lasers.add(((LaserProfileLayer) layer).laserProfile);
        }
    }

    @Override
    public void removeLayer(@NonNull ChartLayer layer) {
        super.removeLayer(layer);
        // Remove from track/laser lists
        if (layer instanceof TrackProfileLayer) {
            tracks.remove(((TrackProfileLayer) layer).trackData);
        } else if (layer instanceof LaserProfileLayer) {
            lasers.remove(((LaserProfileLayer) layer).laserProfile);
        }
    }

    /**
     * Performs a search for the nearest data point
     */
    @NonNull
    private ChartFocusEvent findClosest(double x, double y) {
        ChartFocusEvent closest = new ChartFocusEvent.Unfocused();
        double closestDistance = Double.POSITIVE_INFINITY;
        for (TrackData trackData : tracks) {
            if (!trackData.data.isEmpty()) {
                final MLocation start = trackData.data.get(0);
                for (MLocation loc : trackData.data) {
                    final double dx = start.distanceTo(loc) - x;
                    final double dy = loc.altitude_gps - start.altitude_gps - y;
                    final double distance = dx * dx + dy * dy; // distance squared
                    if (distance < closestDistance) {
                        closest = new ChartFocusEvent.TrackFocused(loc, trackData.data);
                        closestDistance = distance;
                    }
                }
            }
        }
        for (LaserProfile laser : lasers) {
            for (LaserMeasurement point : laser.points) {
                final double dx = point.x - x;
                final double dy = point.y - y;
                final double distance = dx * dx + dy * dy; // distance squared
                if (distance < closestDistance) {
                    closest = new ChartFocusEvent.LaserFocused(point);
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        dataBounds.x.max /= scaleFactor;
        dataBounds.y.min /= scaleFactor;
        return super.getBounds(dataBounds, axis);
    }

}
